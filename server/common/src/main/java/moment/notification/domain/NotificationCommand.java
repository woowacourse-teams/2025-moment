package moment.notification.domain;

public record NotificationCommand(
        Long userId,
        NotificationType notificationType,
        SourceData sourceData,
        PushNotificationMessage pushMessage
) {
}
