package moment.notification.infrastructure.expo;

import java.util.Map;
import moment.notification.domain.PushNotificationMessage;

public record ExpoPushMessage(
        String to,
        String title,
        String body,
        Map<String, Object> data,
        String sound
) {
    public static ExpoPushMessage of(String token, PushNotificationMessage message,
                                     Map<String, Object> data) {
        return new ExpoPushMessage(token, message.getTitle(), message.getBody(), data, "default");
    }
}
