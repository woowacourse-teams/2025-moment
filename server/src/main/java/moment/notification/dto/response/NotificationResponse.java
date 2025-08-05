package moment.notification.dto.response;

import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;

public record NotificationResponse(
        NotificationType notificationType,
        TargetType targetType,
        Long targetId,
        String message,
        boolean isRead
) {

    public static NotificationResponse createSseResponse(
            NotificationType notificationType,
            TargetType targetType,
            Long targetId
    ) {
        return new NotificationResponse(notificationType, targetType, targetId, notificationType.getMessage(), false);
    }
}
