package moment.notification.domain;

import moment.global.domain.TargetType;

public record NotificationCommand(
        Long userId,
        Long targetId,
        NotificationType notificationType,
        TargetType targetType,
        Long groupId,
        PushNotificationMessage pushMessage
) {
}
