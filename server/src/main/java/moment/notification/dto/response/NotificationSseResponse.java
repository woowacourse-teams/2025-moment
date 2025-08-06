package moment.notification.dto.response;

import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;

public record NotificationSseResponse(
        NotificationType notificationType,
        TargetType targetType,
        Long targetId,
        String message,
        boolean isRead
) {

    public static NotificationSseResponse createSseResponse(
            NotificationType notificationType,
            TargetType targetType,
            Long targetId
    ) {
        return new NotificationSseResponse(notificationType, targetType, targetId, notificationType.getMessage(),
                false);
    }
}
