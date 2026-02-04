# Phase 4: 서비스 레이어 리팩토링

---

## Step 4.1: NotificationCommand 도입

**`notification/domain/NotificationCommand.java`**
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

### NotificationFacadeService 변경

```java
// Before: 2개 메서드 (6/7 파라미터)
createNotificationAndSendSse(userId, targetId, type, targetType, groupId)
createNotificationAndSendSseAndPush(userId, targetId, type, targetType, groupId, message)

// After: 단일 메서드
public void notify(NotificationCommand command) {
    Notification saved = notificationApplicationService.createNotification(
        command.userId(), command.targetId(), command.notificationType(),
        command.targetType(), command.groupId());

    NotificationPayload payload = NotificationPayload.from(saved);

    sseNotificationService.sendToClient(command.userId(), "notification",
        NotificationSseResponse.of(payload));

    if (command.pushMessage() != null) {
        pushNotificationApplicationService.sendToDeviceEndpoint(
            command.userId(), command.pushMessage(), payload);
    }
}
```

### NotificationEventHandler 변경

```java
// Before (7개 핸들러 각각 6개 파라미터 전달)
notificationFacadeService.createNotificationAndSendSseAndPush(
    event.momenterId(), event.momentId(),
    NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
    null, PushNotificationMessage.REPLY_TO_MOMENT);

// After
notificationFacadeService.notify(new NotificationCommand(
    event.momenterId(), event.momentId(),
    NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
    null, PushNotificationMessage.REPLY_TO_MOMENT));
```

---

## Step 4.2: NotificationType <-> PushNotificationMessage 매핑

**`notification/domain/PushNotificationMessage.java`** -- 매핑 메서드 추가:
```java
public static PushNotificationMessage from(NotificationType type) {
    return switch (type) {
        case NEW_COMMENT_ON_MOMENT -> REPLY_TO_MOMENT;
        case GROUP_JOIN_REQUEST -> GROUP_JOIN_REQUEST;
        case GROUP_JOIN_APPROVED -> GROUP_JOIN_APPROVED;
        case GROUP_KICKED -> GROUP_KICKED;
        case MOMENT_LIKED -> MOMENT_LIKED;
        case COMMENT_LIKED -> COMMENT_LIKED;
    };
}
```

### TDD 테스트
```
1. 모든_NotificationType에_대해_PushNotificationMessage가_매핑된다
2. 매핑되지_않는_타입이_있으면_컴파일_에러_발생 (switch exhaustiveness)
```