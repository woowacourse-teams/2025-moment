# Phase 3: 알림 페이로드 통일

> SSE와 Push가 동일한 데이터 구조를 사용.

---

## Step 3.1: NotificationPayload VO 생성

**`notification/domain/NotificationPayload.java`**
```java
public record NotificationPayload(
    Long notificationId,
    NotificationType notificationType,
    TargetType targetType,
    Long targetId,
    Long groupId,
    String message,
    String link
) {
    public static NotificationPayload from(Notification notification) {
        return new NotificationPayload(
            notification.getId(),
            notification.getNotificationType(),
            notification.getTargetType(),
            notification.getTargetId(),
            notification.getGroupId(),
            notification.getNotificationType().getMessage(),
            buildLink(notification.getTargetType(), notification.getTargetId(),
                      notification.getGroupId())
        );
    }

    private static String buildLink(TargetType targetType, Long targetId, Long groupId) {
        return switch (targetType) {
            case MOMENT -> "/moments/" + targetId;
            case COMMENT -> "/comments/" + targetId;
            case GROUP, GROUP_MEMBER -> "/groups/" + (groupId != null ? groupId : targetId);
        };
    }
}
```

### TDD 테스트 목록 -- `NotificationPayloadTest.java`
```
1. MOMENT_타겟으로_생성_시_moments_링크를_생성한다
2. GROUP_타겟으로_생성_시_groups_링크를_생성한다
3. COMMENT_타겟으로_생성_시_comments_링크를_생성한다
4. NotificationType_메시지를_포함한다
```

---

## Step 3.2: NotificationSseResponse 개선

**`notification/dto/response/NotificationSseResponse.java`**
```java
// Before
public static NotificationSseResponse createSseResponse(...)

// After: 프로젝트 컨벤션 + link 필드 추가
@Schema(description = "딥링크", example = "/moments/1")
String link
// ...
public static NotificationSseResponse of(NotificationPayload payload) {
    return new NotificationSseResponse(
        payload.notificationId(), payload.notificationType(),
        payload.targetType(), payload.targetId(), payload.groupId(),
        payload.message(), false, payload.link()
    );
}
```

---

## Step 3.3: Facade 통합 페이로드 적용

**`notification/service/facade/NotificationFacadeService.java`**
```java
// Before: SSE용/Push용 별도 구성
// After: NotificationPayload 한 번 생성, SSE와 Push에 공유

Notification saved = notificationApplicationService.createNotification(...);
NotificationPayload payload = NotificationPayload.from(saved);

// SSE
sseNotificationService.sendToClient(userId, "notification",
    NotificationSseResponse.of(payload));

// Push (data 필드에 payload 포함)
pushNotificationApplicationService.sendToDeviceEndpoint(userId, message, payload);
```

**`PushNotificationApplicationService.sendToDeviceEndpoint()`** -- `NotificationPayload`를 받아 Push `data` 필드로 전달