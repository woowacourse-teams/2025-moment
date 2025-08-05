package moment.notification.dto.response;

import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;

public record NotificationResponse(
        Long id,
        NotificationType notificationType,
        TargetType targetType,
        Long targetId,
        String message,
        boolean isRead
) {

    public static NotificationResponse from(Notification notification) {
        NotificationType notificationType = notification.getNotificationType();
        return new NotificationResponse(
                notification.getId(),
                notificationType,
                notification.getTargetType(),
                notification.getTargetId(),
                notificationType.getMessage(), notification.isRead()
        );
    }
}
