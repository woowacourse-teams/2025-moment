# 차세대 알림 시스템 구현 계획 (FCM → Expo Push + SSE 개선)

**Status**: PLAN
**Created**: 2026-02-04
**Complexity**: HIGH

---

## 목표

React Native(Expo) WebView 하이브리드 앱 환경에 최적화된 알림 시스템으로 전환한다.
- FCM → Expo Push Notification Service 마이그레이션
- SSE 다중 세션 지원 (WebView + 웹 브라우저 동시 접속)
- 기존 Dead Code 정리 및 버그 수정 (Tidy First)
- 알림 페이로드 통일 (SSE/Push 동일 구조)

### 환경별 알림 전략

| 환경 | 상태 | 채널 | UX |
|---|---|---|---|
| 모바일 앱 (WebView) | 백그라운드/종료 | Expo Push (OS) | 시스템 배너 |
| 모바일 앱 (WebView) | 포그라운드 | SSE (Web) | 알림 숫자/목록만 갱신 |
| 웹 브라우저 | 포그라운드 | SSE (Web) | 토스트 팝업 + 숫자/목록 갱신 |

### 서버 발송 전략: 병렬 발송

1. 이벤트 발생 → DB 저장
2. SSE 발송 (모든 활성 세션)
3. Expo Push 발송 (모든 디바이스 토큰)
4. Expo 영수증 API로 유효하지 않은 토큰 정기 정리

---

## Phase 1: Dead Code 정리 & 버그 수정 (Tidy First - 구조적 변경만)

> 원칙: 동작 변경 없이 코드 정리만 수행. 각 스텝별 별도 커밋.

### Step 1.1: Dead Code 삭제

**삭제할 파일:**
- `src/main/java/moment/comment/dto/EchoCreateEvent.java` — 발행처/핸들러 없는 Dead Event
- `src/main/java/moment/notification/service/EmailNotificationService.java` — 전체 주석 처리된 미사용 코드

**수정할 파일:**
- `src/main/java/moment/notification/domain/NotificationType.java`
  - `NEW_REPLY_ON_COMMENT` enum 값 삭제 (어디서도 사용 안 됨)

**수정할 테스트:**
- `src/test/java/moment/notification/service/application/NotificationApplicationServiceTest.java`
- `src/test/java/moment/notification/service/notification/NotificationServiceTest.java`
  - `NEW_REPLY_ON_COMMENT` 참조 제거

### Step 1.2: `checkNotification()` → `markAsRead()` 리네이밍

**수정할 파일:**
- `notification/domain/Notification.java` — 메서드명 변경
- `notification/service/notification/NotificationService.java` — 호출부 변경
- 관련 테스트 파일 3개 업데이트

### Step 1.3: 중복 save 메서드 통합 (H4)

**수정할 파일:**
- `notification/service/notification/NotificationService.java`
  - `saveNotificationWithNewTransaction()` + `saveNotificationWithGroupId()` → 단일 `save(User, Long targetId, NotificationType, TargetType, Long groupId)` 메서드로 통합
- `notification/service/application/NotificationApplicationService.java`
  - `createNotification()` 내부 분기 제거

### Step 1.4: 타입 일관성 수정 (H5)

**수정할 파일:**
- `notification/domain/PushNotification.java` — `long id` → `Long id`
- `notification/infrastructure/NotificationRepository.java` — `Boolean` → `boolean` 통일

### Step 1.5: Repository @Transactional 제거 (아키텍처 규칙)

**수정할 파일:**
- `notification/infrastructure/PushNotificationRepository.java`
  - `deleteByDeviceEndpoint()`의 `@Transactional` 제거 (서비스 레이어에서 관리)

### Step 1.6: 로깅 일관성 (L1)

**수정할 파일:**
- `notification/service/eventHandler/NotificationEventHandler.java`
  - `handleCommentCreateEvent()`에 `log.info()` 추가 (다른 6개 핸들러와 동일 패턴)

---

## Phase 2: Push 인프라 마이그레이션 (FCM → Expo Push)

> 핵심 행동적 변경. TDD 사이클 적용.

### Step 2.1: Expo Push API 클라이언트 생성

**생성할 파일:**
- `notification/infrastructure/expo/ExpoPushApiClient.java`
  - Spring `RestClient`로 Expo HTTP API 호출
  - `POST https://exp.host/--/api/v2/push/send` (배치 최대 100건)
  - `POST https://exp.host/--/api/v2/push/getReceipts` (영수증 조회)
  - **HTTP 레벨 즉시 재시도** (네트워크 오류, 429 Rate Limit, 5xx 서버 오류)
    - 최대 3회, 지수 백오프 (1초 → 2초 → 4초)
    - 429 응답 시 `Retry-After` 헤더 존중
    - 4xx (429 제외) 응답은 영구 실패로 판단하여 재시도하지 않음

- `notification/infrastructure/expo/ExpoPushMessage.java`
  - record: `to`, `title`, `body`, `data` (Map), `sound`, `badge`

- `notification/infrastructure/expo/ExpoPushTicket.java`
  - record: `id`, `status`, `message`, `details`

- `notification/infrastructure/expo/ExpoPushReceipt.java`
  - record: `status`, `message`, `details`

**생성할 테스트:**
- `test/.../infrastructure/expo/ExpoPushApiClientTest.java`

### Step 2.2: ExpoPushNotificationSender 구현

**생성할 파일:**
- `notification/infrastructure/expo/ExpoPushNotificationSender.java`
  - `PushNotificationSender` 인터페이스 구현 (기존 Strategy 패턴 활용)
  - `PushNotificationRepository`에서 유저의 모든 디바이스 토큰 조회
  - `ExpoPushApiClient`로 배치 발송
  - 발송 성공 → 티켓 ID를 `expo_push_tickets` 테이블에 `PENDING` 상태로 저장
  - **발송 실패 처리 (HTTP 재시도 모두 실패한 경우)**:
    - `expo_push_tickets` 테이블에 `SEND_FAILED` 상태로 저장 (`ticket_id` = null)
    - `pushNotificationMessage` (enum 이름) + `notificationId` + `deviceEndpoint`를 저장하여 스케줄러가 메시지 재구성 가능
    - `ExpoPushReceiptService`의 스케줄러가 `SEND_FAILED` 건을 감지하여 지연 재발송 처리

**생성할 테스트:**
- `test/.../infrastructure/expo/ExpoPushNotificationSenderTest.java`

### Step 2.3: Expo 영수증 체크 + 발송 실패 재시도 서비스 — DB 테이블 저장 방식

**DB 마이그레이션:**
- `src/main/resources/db/migration/mysql/V36__create_expo_push_tickets__mysql.sql`
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

**status 상태 흐름:**
```
발송 성공 → PENDING (영수증 대기) → 영수증 확인 성공 → 삭제
                                  → DeviceNotRegistered → 토큰 삭제 + 티켓 삭제
                                  → MessageRateExceeded → RECEIPT_RETRY → 재확인

발송 실패 → SEND_FAILED → 재발송 시도 성공 → PENDING
                        → 재발송 시도 실패 (retry_count < max_retries) → SEND_FAILED (retry_count++)
                        → 재발송 시도 실패 (retry_count >= max_retries) → DEAD → 7일 후 삭제
```

**생성할 파일:**
- `notification/domain/ExpoPushTicketRecord.java`
  - JPA 엔티티
  - 필드: `ticketId`(nullable — 발송 실패 시 null), `deviceEndpoint`, `user`, `notificationId`(nullable), `pushNotificationMessage`(enum 참조), `status`, `retryCount`, `maxRetries`, `nextRetryAt`, `createdAt`
  - 재시도 시 `pushNotificationMessage` enum에서 title/body 복원, `notificationId`로 동적 data(딥링크 등) 재구성
  - soft delete 미적용 (처리 완료 후 hard delete)

- `notification/domain/ExpoPushTicketStatus.java`
  - enum: `PENDING`, `SEND_FAILED`, `RECEIPT_RETRY`, `DEAD`

- `notification/infrastructure/expo/ExpoPushTicketRepository.java`
  - `findAllByStatus(ExpoPushTicketStatus status)` — 상태별 티켓 조회
  - `findAllByStatusAndNextRetryAtBefore(ExpoPushTicketStatus status, LocalDateTime now)` — 재시도 시각이 도래한 티켓 조회
  - `deleteAllByStatusAndCreatedAtBefore(ExpoPushTicketStatus status, LocalDateTime before)` — 오래된 티켓 정리

- `notification/service/notification/ExpoPushReceiptService.java`
  - `@Scheduled(fixedDelay = 1800000)` (30분 간격)으로 2가지 작업 수행:

  - **(1) 발송 실패 재시도** (`SEND_FAILED` + `nextRetryAt` 도래):
    - `pushNotificationMessage` enum에서 title/body 복원 + `notificationId`로 동적 data 재구성 → `ExpoPushMessage` 생성
    - `ExpoPushApiClient`로 재발송
    - 성공 → `PENDING`으로 변경 + `ticketId` 업데이트
    - 실패 → `retryCount++`, `nextRetryAt` = 지수 백오프 (5분 × 2^retryCount)
    - `retryCount >= maxRetries` → `DEAD` (최종 포기)

  - **(2) 영수증 확인** (`PENDING` 상태):
    - Expo Receipt API 호출 → 결과 처리
    - `ok` → 티켓 삭제
    - `DeviceNotRegistered` → 해당 디바이스 토큰 soft delete + 티켓 삭제
    - `MessageRateExceeded` → `RECEIPT_RETRY`, `nextRetryAt` 설정 후 다음 주기에 재확인
    - 기타 일시적 오류 → `retryCount++` 후 재시도

  - **(3) 만료 정리**:
    - `DEAD` 상태 + 7일 경과 → hard delete
    - `PENDING`/`RECEIPT_RETRY` + 3일 경과 → hard delete (비정상 상태 방지)

**재시도 간격 (지수 백오프):**
| retryCount | nextRetryAt |
|---|---|
| 0 (최초 실패) | 5분 후 |
| 1 | 10분 후 |
| 2 | 20분 후 |
| 3 (maxRetries 도달) | DEAD 전환 |

**생성할 테스트:**
- `test/.../service/notification/ExpoPushReceiptServiceTest.java`
  - 발송 실패 재시도 성공/실패/최종 포기 시나리오
  - 영수증 확인 성공/DeviceNotRegistered/MessageRateExceeded 시나리오
  - 만료 정리 시나리오
- `test/.../infrastructure/expo/ExpoPushTicketRepositoryTest.java`

### Step 2.4: PushNotificationMessage 개선 (L2)

**수정할 파일:**
- `notification/domain/PushNotificationMessage.java`
  - 하드코딩 `"[moment]"` → `"Moment"` (또는 설정값 주입)
  - `sound` 필드 추가 (`"default"` — Expo 포맷)

### Step 2.5: DeviceEndpointRequest Expo 토큰 대응

**수정할 파일:**
- `notification/dto/request/DeviceEndpointRequest.java`
  - `@Schema` example을 Expo 토큰 형식으로 변경: `ExponentPushToken[xxx]`
  - 기존 `@NotBlank` 유지 (토큰 포맷 검증은 Expo API가 담당)

### Step 2.6: FCM 인프라 제거

**삭제할 파일:**
- `notification/infrastructure/FcmPushNotificationSender.java`
- `global/config/FcmConfig.java`

**삭제할 테스트:**
- `test/.../infrastructure/FcmPushNotificationSenderTest.java`

**수정할 파일:**
- `build.gradle`
  - 제거: `implementation 'com.google.firebase:firebase-admin:9.5.0'`
  - 추가 의존성 없음 (Spring `RestClient` 내장 사용)
- `src/main/resources/application-dev.yml` — `fcm:` 섹션 제거
- `src/main/resources/application-prod.yml` — `fcm:` 섹션 제거
- `src/test/resources/application-test.yml` — `fcm:` 섹션 제거

**push_notifications 테이블:** 변경 불필요. `device_endpoint` (VARCHAR 255)은 Expo 토큰도 저장 가능.
**expo_push_tickets 테이블:** Step 2.3에서 생성 (V36 마이그레이션).

### Step 2.7: 설정 추가

**수정할 파일:**
- `application-dev.yml`, `application-prod.yml`
  ```yaml
  expo:
    push:
      enabled: true
  ```
- `application-test.yml`
  ```yaml
  expo:
    push:
      enabled: false
  ```

---

## Phase 3: 알림 페이로드 통일

> SSE와 Push가 동일한 데이터 구조를 사용하여 클라이언트 처리 단순화.

### Step 3.1: 통합 페이로드 VO 생성

**생성할 파일:**
- `notification/domain/NotificationPayload.java`
  ```java
  public record NotificationPayload(
      Long notificationId,
      NotificationType notificationType,
      TargetType targetType,
      Long targetId,
      Long groupId,
      String message,
      String link    // 딥링크: "/moments/{id}", "/groups/{groupId}" 등
  ) {
      public static NotificationPayload from(Notification notification) { ... }
  }
  ```

### Step 3.2: NotificationSseResponse 개선

**수정할 파일:**
- `notification/dto/response/NotificationSseResponse.java`
  - `link` 필드 추가
  - `createSseResponse()` → `of()` 팩토리 메서드명 변경 (프로젝트 컨벤션)

### Step 3.3: Facade 통합 페이로드 사용

**수정할 파일:**
- `notification/service/facade/NotificationFacadeService.java`
  - `NotificationPayload` 생성 후 SSE와 Push 양쪽에 동일 데이터 전달
  - Push의 `data` 필드에 페이로드 포함 (클라이언트 딥링크 처리용)

---

## Phase 4: 서비스 레이어 리팩토링

### Step 4.1: NotificationCommand 도입 (파라미터 객체화)

**생성할 파일:**
- `notification/domain/NotificationCommand.java`
  ```java
  public record NotificationCommand(
      Long userId,
      Long targetId,
      NotificationType notificationType,
      TargetType targetType,
      Long groupId,
      PushNotificationMessage pushMessage
  ) {}
  ```

**수정할 파일:**
- `notification/service/facade/NotificationFacadeService.java`
  - 기존 2개 메서드(6/7 파라미터) → 단일 `notify(NotificationCommand command)` 메서드
  - Push 메시지가 null이면 Push 발송 스킵
- `notification/service/eventHandler/NotificationEventHandler.java`
  - 모든 핸들러에서 `NotificationCommand` 구성 후 `notify()` 호출

### Step 4.2: NotificationType ↔ PushNotificationMessage 매핑 보장 (M3)

**수정할 파일:**
- `notification/domain/PushNotificationMessage.java`
  - `static PushNotificationMessage from(NotificationType type)` 매핑 메서드 추가
  - 핸들러에서 NotificationType만 지정하면 PushMessage가 자동 결정

---

## Phase 5: 이벤트 시스템 개선

### Step 5.1: Self-Notification 방지 일원화 (H1)

**전략**: Publisher 측에서 일관되게 방지 (Like 서비스 패턴 따름)

**수정할 파일:**
- `comment/service/facade/CommentCreateFacadeService.java`
  - `publisher.publishEvent()` 호출 전에 `moment.getMomenter().getId().equals(userId)` 체크 추가
- `notification/service/eventHandler/NotificationEventHandler.java`
  - `handleGroupCommentCreateEvent()`의 자기 알림 체크 제거 (Publisher로 이동)

### Step 5.2: GroupCommentCreateEvent 발행 완성 (H2)

**생성할 파일:**
- `comment/service/facade/GroupCommentCreateFacadeService.java`
  - `CommentApplicationService.createCommentInGroup()` 래핑
  - `GroupCommentCreateEvent` 발행 + self-notification 방지

**수정할 파일:**
- `group/presentation/GroupCommentController.java`
  - `CommentApplicationService` 직접 호출 → `GroupCommentCreateFacadeService` 사용으로 변경

### Step 5.3: handleCommentCreateEvent Self-Notification 방지

**수정할 파일:**
- `comment/dto/CommentCreateEvent.java`
  - `commenterId` 필드 추가 (Self-notification 체크에 필요)
- `comment/service/facade/CommentCreateFacadeService.java`
  - 이벤트 생성 시 `commenterId(userId)` 포함
  - Publisher 측에서 self-notification 체크 후 이벤트 발행

---

## Phase 6: SSE 다중 세션 지원

> WebView + 웹 브라우저 동시 접속 시 모든 세션에 알림 전달

### Step 6.1: Emitters 다중 연결 지원

**수정할 파일:**
- `notification/infrastructure/Emitters.java`
  - `Map<Long, SseEmitter>` → `Map<Long, List<SseEmitter>>`
  - `add()`: 사용자별 emitter 리스트에 추가
  - `sendToClient()`: 해당 사용자의 모든 emitter에 전송
  - `sendHeartbeat()`: 모든 emitter에 하트비트

- `notification/service/notification/SseNotificationService.java`
  - `subscribe()` 수정: 기존 연결 대체 → 추가 방식으로 변경

**수정할 테스트:**
- `test/.../infrastructure/EmittersTest.java`
- `test/.../service/notification/SseNotificationServiceTest.java`

---

## 구현 순서 및 의존 관계

```
Phase 1 (구조 정리) ─────────── 독립, 먼저 실행
    │
    ▼
Phase 2 (FCM → Expo) ────────── Phase 1 완료 필요 (깨끗한 코드베이스)
    │
    ├──▶ Phase 3 (페이로드 통일) ── Phase 2 완료 필요 (새 sender 존재)
    │
    ├──▶ Phase 5 (이벤트 개선) ─── Phase 1 완료 필요 (Dead Code 정리 후)
    │
    └──▶ Phase 6 (SSE 개선) ───── 독립 실행 가능

Phase 4 (서비스 리팩토링) ────── Phase 3 완료 필요 (통합 페이로드)
```

---

## 파일 변경 요약

| Phase | 생성 | 수정 | 삭제 | DB 마이그레이션 |
|-------|-----|------|------|---------------|
| 1 | 0 | ~10 | 2 | 0 |
| 2 | 10 (코드 6 + 엔티티 1 + enum 1 + 레포 1 + 마이그레이션 1) | ~8 | 3 | V36 (expo_push_tickets) |
| 3 | 1 | 3 | 0 | 0 |
| 4 | 1 | 3 | 0 | 0 |
| 5 | 1 | 4 | 0 | 0 |
| 6 | 0 | 3 | 0 | 0 |
| **합계** | **13** | **~31** | **5** | **1** |

## 의존성 변경 (build.gradle)

**제거:**
```groovy
implementation 'com.google.firebase:firebase-admin:9.5.0'
```

**추가:** 없음. Spring Boot 3.x 내장 `RestClient` 사용.

---

## 검증 계획

### 단위 테스트
- [ ] `ExpoPushApiClientTest` — Expo API 호출/응답 + HTTP 레벨 재시도 검증 (네트워크 오류, 429, 5xx)
- [ ] `ExpoPushNotificationSenderTest` — 토큰 조회 + 배치 발송 + 발송 실패 시 SEND_FAILED 저장 검증
- [ ] `ExpoPushReceiptServiceTest` — 영수증 체크 + 토큰 정리 + 발송 실패 재시도 + 최종 포기(DEAD) 검증
- [ ] `NotificationFacadeServiceTest` — 통합 페이로드 + 병렬 발송 검증
- [ ] `EmittersTest` — 다중 세션 추가/제거/전송 검증
- [ ] `GroupCommentCreateFacadeServiceTest` — 이벤트 발행 + self-notification 방지

### 통합 테스트
- [ ] `./gradlew fastTest` — 전체 단위/통합 테스트 통과
- [ ] `./gradlew e2eTest` — E2E 테스트 통과

### 수동 검증
- [ ] SSE 구독 후 알림 발생 → 실시간 수신 확인
- [ ] 다중 탭 SSE 구독 → 모든 탭에서 수신 확인
- [ ] Expo Push 토큰 등록 → 푸시 발송 → 디바이스 수신 확인
- [ ] 유효하지 않은 토큰 등록 → 영수증 체크 → 자동 정리 확인
- [ ] Expo API 일시 장애 시뮬레이션 → SEND_FAILED 저장 → 스케줄러 재시도 → 최종 발송 확인
- [ ] maxRetries 초과 → DEAD 전환 → 7일 후 정리 확인
- [ ] self-notification 방지 동작 확인 (자기 글에 자기가 댓글)