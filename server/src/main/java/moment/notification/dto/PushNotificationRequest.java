package moment.notification.dto;

public record PushNotificationRequest(Long userId, String title, String body) {
}
