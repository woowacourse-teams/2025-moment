# Phase 1: Dead Code 정리 & 버그 수정

> Tidy First -- 동작 변경 없이 구조만 정리. 각 Step 별도 커밋.

---

## Step 1.1: Dead Code 삭제

### 삭제할 파일
| 파일 | 사유 |
|------|------|
| `comment/dto/EchoCreateEvent.java` | 발행처 없음, 핸들러 없음 -- 완전 Dead Code |
| `notification/service/EmailNotificationService.java` | 전체 주석 처리 (135줄) -- 미사용 |

### 수정할 파일

**`notification/domain/NotificationType.java`**
```java
// 삭제할 enum 값:
NEW_REPLY_ON_COMMENT("내 코멘트에 새로운 답장이 달렸습니다."),
```
- 어디서도 사용되지 않음 (핸들러 없음, 이벤트 없음)

**`notification/service/application/NotificationApplicationServiceTest.java`**
- `NEW_REPLY_ON_COMMENT` 참조하는 테스트 데이터 변경 -> 다른 활성 enum으로 대체

**`notification/service/notification/NotificationServiceTest.java`**
- 동일하게 `NEW_REPLY_ON_COMMENT` 참조 제거

### 검증
```bash
./gradlew fastTest
```

---

## Step 1.2: `checkNotification()` -> `markAsRead()` 리네이밍

### 변경 사항

**`notification/domain/Notification.java`** (line 76)
```java
// Before
public void checkNotification() { isRead = true; }

// After
public void markAsRead() { isRead = true; }
```

**`notification/service/notification/NotificationService.java`** (lines 63, 70)
```java
// Before
notification.checkNotification();
// After
notification.markAsRead();
```

### 테스트 파일 수정
- `notification/domain/NotificationTest.java` -- `checkNotification()` -> `markAsRead()`
- `notification/service/notification/NotificationServiceTest.java` -- 동일
- `notification/service/application/NotificationApplicationServiceTest.java` -- 동일

---

## Step 1.3: 중복 save 메서드 통합

### 현재 코드 (NotificationService.java)
```java
// 메서드 1: groupId 없음
public Notification saveNotificationWithNewTransaction(User user, Long targetId,
    NotificationType type, TargetType targetType) {
    return notificationRepository.save(new Notification(user, type, targetType, targetId));
}

// 메서드 2: groupId 있음 -- 거의 동일
public Notification saveNotificationWithGroupId(User user, Long targetId,
    NotificationType type, TargetType targetType, Long groupId) {
    return notificationRepository.save(new Notification(user, type, targetType, targetId, groupId));
}
```

### 변경 후
```java
@Transactional
public Notification save(User user, Long targetId, NotificationType notificationType,
                         TargetType targetType, Long groupId) {
    Notification notification = (groupId != null)
        ? new Notification(user, notificationType, targetType, targetId, groupId)
        : new Notification(user, notificationType, targetType, targetId);
    return notificationRepository.save(notification);
}
```

**`notification/service/application/NotificationApplicationService.java`** -- `createNotification()` 단순화:
```java
// Before: if (groupId != null) 분기 2개 메서드 호출
// After: 단일 메서드 호출
return notificationService.save(user, targetId, notificationType, targetType, groupId);
```

---

## Step 1.4: 타입 일관성 수정

**`notification/domain/PushNotification.java`** (line 32)
```java
// Before
private long id;
// After
private Long id;
```

**`notification/infrastructure/NotificationRepository.java`**
```java
// Before: Boolean (nullable wrapper)
List<Notification> findAllByUserIdAndIsRead(Long userId, Boolean isRead);
// After: boolean (primitive, entity 필드와 일치)
List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);
```

**영향 범위**: `NotificationApplicationService.getNotificationBy()` -- `Boolean read` 파라미터 유지 (컨트롤러에서 nullable로 받음). 호출 시 unboxing 처리.

---

## Step 1.5: Repository @Transactional 제거

**`notification/infrastructure/PushNotificationRepository.java`** (line 13)
```java
// Before
@Transactional
void deleteByDeviceEndpoint(String deviceEndpoint);
// After (서비스 레이어에서 트랜잭션 관리)
void deleteByDeviceEndpoint(String deviceEndpoint);
```

**영향**: `FcmPushNotificationSender.addSendCallback()` -- Phase 2에서 삭제 예정이므로 영향 없음.

---

## Step 1.6: 로깅 일관성

**`notification/service/eventHandler/NotificationEventHandler.java`** (line 30)
```java
// Before: log.info() 없음
public void handleCommentCreateEvent(CommentCreateEvent event) {
    notificationFacadeService.createNotificationAndSendSseAndPush(...);
}

// After: 다른 6개 핸들러와 동일 패턴
public void handleCommentCreateEvent(CommentCreateEvent event) {
    log.info("CommentCreateEvent received: momentId={}, momenterId={}",
        event.momentId(), event.momenterId());
    notificationFacadeService.createNotificationAndSendSseAndPush(...);
}
```