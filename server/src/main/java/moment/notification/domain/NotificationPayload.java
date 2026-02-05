package moment.notification.domain;

import moment.global.domain.TargetType;

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
