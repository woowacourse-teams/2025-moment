# Phase 2: Push 인프라 마이그레이션 (FCM -> Expo Push)

> TDD 사이클 적용. 테스트 먼저 작성 -> 최소 구현 -> 리팩토링

---

## Step 2.1: Expo Push API 클라이언트

### 생성할 파일

**`notification/infrastructure/expo/ExpoPushMessage.java`**
```java
public record ExpoPushMessage(
    String to,          // ExponentPushToken[xxx]
    String title,
    String body,
    Map<String, Object> data,
    String sound        // "default"
) {
    public static ExpoPushMessage of(String token, PushNotificationMessage message,
                                      Map<String, Object> data) {
        return new ExpoPushMessage(token, message.getTitle(), message.getBody(), data, "default");
    }
}
```

**`notification/infrastructure/expo/ExpoPushTicket.java`**
```java
public record ExpoPushTicket(String id, String status, String message,
                              Map<String, Object> details) {
    public boolean isOk() { return "ok".equals(status); }
    public boolean isError() { return "error".equals(status); }
}
```

**`notification/infrastructure/expo/ExpoPushReceipt.java`**
```java
public record ExpoPushReceipt(String status, String message,
                               Map<String, Object> details) {
    public boolean isOk() { return "ok".equals(status); }
    public boolean isDeviceNotRegistered() {
        return details != null && "DeviceNotRegistered".equals(details.get("error"));
    }
    public boolean isMessageRateExceeded() {
        return details != null && "MessageRateExceeded".equals(details.get("error"));
    }
}
```

**`notification/infrastructure/expo/ExpoPushApiClient.java`**
- Spring `RestClient` 사용
- **HTTP 즉시 재시도**: 네트워크 오류, 429, 5xx -> 최대 3회, 지수 백오프 (1s -> 2s -> 4s)
- 429 -> `Retry-After` 헤더 존중
- 4xx (429 제외) -> 영구 실패, 재시도 안 함
- Expo API 엔드포인트:
  - `POST https://exp.host/--/api/v2/push/send`
  - `POST https://exp.host/--/api/v2/push/getReceipts`

### TDD 테스트 목록 -- `ExpoPushApiClientTest.java`
```
1. 단일_메시지_발송_성공_시_티켓을_반환한다
2. 배치_메시지_발송_성공_시_티켓_리스트를_반환한다
3. 네트워크_오류_시_최대_3회_재시도_후_예외를_던진다
4. 서버_5xx_응답_시_재시도한다
5. 429_응답_시_Retry_After_헤더를_존중하여_재시도한다
6. 4xx_응답_시_재시도_없이_즉시_예외를_던진다
7. 영수증_조회_성공_시_영수증_맵을_반환한다
8. 빈_티켓_ID_목록으로_영수증_조회_시_빈_맵을_반환한다
```

---

## Step 2.2: ExpoPushNotificationSender

### 생성할 파일

**`notification/infrastructure/expo/ExpoPushNotificationSender.java`**
- `PushNotificationSender` 인터페이스 구현 (기존 Strategy 패턴 유지)
- `send(PushNotificationCommand)` 메서드:
  1. `pushNotificationRepository.findByUserId(userId)` -- 디바이스 토큰 조회
  2. 토큰이 없으면 return
  3. 각 토큰에 대해 `ExpoPushMessage` 생성
  4. `expoPushApiClient.send(messages)` -- 배치 발송
  5. 성공한 티켓 -> `PENDING` 상태로 `expo_push_tickets` 저장 (`pushNotificationMessage` enum 이름 + `notificationId`)
  6. 발송 실패 (HTTP 재시도 모두 실패) -> `SEND_FAILED` 상태로 저장 (`ticketId` = null)

### TDD 테스트 목록 -- `ExpoPushNotificationSenderTest.java`
```
1. 디바이스_토큰이_있으면_Expo_API로_발송한다
2. 디바이스_토큰이_없으면_발송하지_않는다
3. 발송_성공_시_PENDING_티켓을_DB에_저장한다
4. 발송_실패_시_SEND_FAILED_티켓을_DB에_저장한다
5. 여러_디바이스에_배치_발송한다
6. PushNotificationMessage_enum_이름을_티켓에_저장한다
```

---

## Step 2.3: DB 마이그레이션 + 영수증/재시도 서비스

### Flyway 마이그레이션

**`src/main/resources/db/migration/mysql/V36__create_expo_push_tickets__mysql.sql`**
```sql
CREATE TABLE expo_push_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(255),
    device_endpoint VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    notification_id BIGINT,
    push_notification_message VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expo_push_ticket_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_expo_push_ticket_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id),
    INDEX idx_expo_push_tickets_status (status),
    INDEX idx_expo_push_tickets_next_retry_at (next_retry_at),
    INDEX idx_expo_push_tickets_created_at (created_at)
);
```

### 생성할 파일

**`notification/domain/ExpoPushTicketStatus.java`**
```java
public enum ExpoPushTicketStatus {
    PENDING,        // 발송 성공, 영수증 대기
    SEND_FAILED,    // 발송 실패, 재발송 대기
    RECEIPT_RETRY,  // 영수증 일시 오류, 재확인 대기
    DEAD            // 최종 포기 (maxRetries 초과)
}
```

**`notification/domain/ExpoPushTicketRecord.java`**
- JPA 엔티티 (`@Entity(name = "expo_push_tickets")`)
- soft delete 미적용 (처리 완료 시 hard delete)
- 필드: `id`, `ticketId`(nullable), `deviceEndpoint`, `user`(ManyToOne LAZY), `notificationId`(nullable), `pushNotificationMessage`(String -- enum name), `status`(ExpoPushTicketStatus), `retryCount`, `maxRetries`(기본 3), `nextRetryAt`(nullable), `createdAt`

**`notification/infrastructure/expo/ExpoPushTicketRepository.java`**
```java
public interface ExpoPushTicketRepository extends JpaRepository<ExpoPushTicketRecord, Long> {
    List<ExpoPushTicketRecord> findAllByStatus(ExpoPushTicketStatus status);
    List<ExpoPushTicketRecord> findAllByStatusAndNextRetryAtBefore(
        ExpoPushTicketStatus status, LocalDateTime now);
    void deleteAllByStatusAndCreatedAtBefore(ExpoPushTicketStatus status, LocalDateTime before);
}
```

**`notification/service/notification/ExpoPushReceiptService.java`**
- `@Scheduled(fixedDelay = 1800000)` (30분 간격)
- 3가지 작업:

**(1) 발송 실패 재시도** (`SEND_FAILED` + `nextRetryAt` 도래):
```
PushNotificationMessage enum에서 title/body 복원
+ notificationId로 Notification 조회하여 동적 data 재구성
-> ExpoPushMessage 생성 -> ExpoPushApiClient로 재발송
-> 성공: PENDING으로 변경 + ticketId 업데이트
-> 실패: retryCount++, nextRetryAt = 지수 백오프 (5분 x 2^retryCount)
-> retryCount >= maxRetries: DEAD (최종 포기)
```

**(2) 영수증 확인** (`PENDING` 상태):
```
ticketId로 Expo Receipt API 호출
-> ok: 티켓 hard delete
-> DeviceNotRegistered: 디바이스 토큰 soft delete + 티켓 hard delete
-> MessageRateExceeded: RECEIPT_RETRY + nextRetryAt 설정
-> 기타 일시적 오류: retryCount++ 후 재시도
```

**(3) 만료 정리**:
```
DEAD + 7일 경과 -> hard delete
PENDING/RECEIPT_RETRY + 3일 경과 -> hard delete (비정상 방지)
```

### 재시도 간격 (지수 백오프)
| retryCount | nextRetryAt |
|---|---|
| 0 (최초 실패) | 5분 후 |
| 1 | 10분 후 |
| 2 | 20분 후 |
| 3 (maxRetries) | DEAD 전환 |

### TDD 테스트 목록 -- `ExpoPushReceiptServiceTest.java`
```
1. PENDING_티켓의_영수증_확인_성공_시_티켓을_삭제한다
2. DeviceNotRegistered_시_디바이스_토큰과_티켓을_삭제한다
3. MessageRateExceeded_시_RECEIPT_RETRY_상태로_변경한다
4. SEND_FAILED_티켓_재발송_성공_시_PENDING으로_변경한다
5. SEND_FAILED_티켓_재발송_실패_시_retryCount를_증가시킨다
6. maxRetries_초과_시_DEAD_상태로_변경한다
7. DEAD_상태_7일_경과_티켓을_삭제한다
8. PENDING_상태_3일_경과_티켓을_삭제한다
9. nextRetryAt이_도래하지_않은_티켓은_처리하지_않는다
```

---

## Step 2.4: PushNotificationMessage 개선

**`notification/domain/PushNotificationMessage.java`**
```java
// Before
REPLY_TO_MOMENT("[moment]", "당신의 모멘트에 누군가 코멘트를 달았어요:)"),
// After
REPLY_TO_MOMENT("Moment", "당신의 모멘트에 누군가 코멘트를 달았어요:)"),
```
- 모든 enum의 title: `"[moment]"` -> `"Moment"`

---

## Step 2.5: DeviceEndpointRequest 수정

**`notification/dto/request/DeviceEndpointRequest.java`**
```java
// Before
@Schema(description = "디바이스 정보", example = "a1b2c3d4f5")
// After
@Schema(description = "Expo Push Token", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
```

---

## Step 2.6: FCM 인프라 제거

### 삭제할 파일
| 파일 | 사유 |
|------|------|
| `notification/infrastructure/FcmPushNotificationSender.java` | Expo로 대체됨 |
| `global/config/FcmConfig.java` | Firebase 설정 불필요 |
| `test/.../FcmPushNotificationSenderTest.java` | 삭제된 클래스의 테스트 |

### 수정할 파일

**`build.gradle`**
```groovy
// 제거
implementation 'com.google.firebase:firebase-admin:9.5.0'
```

**`application-dev.yml`** -- `fcm:` 섹션 제거
**`application-prod.yml`** -- `fcm:` 섹션 제거
**`application-test.yml`** -- `fcm:` 섹션 제거

---

## Step 2.7: Expo 설정 추가

**`application-dev.yml`, `application-prod.yml`**
```yaml
expo:
  push:
    enabled: true
```

**`application-test.yml`**
```yaml
expo:
  push:
    enabled: false
```