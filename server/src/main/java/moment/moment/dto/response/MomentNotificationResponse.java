package moment.moment.dto.response;

import java.util.List;
import moment.notification.domain.Notification;

public record MomentNotificationResponse(
        boolean isRead,
        List<Long> notificationIds
) {

    public static MomentNotificationResponse from(List<Notification> notifications) {
        boolean isRead = notifications.isEmpty();
        List<Long> ids = notifications.stream()
                .map(Notification::getId)
                .toList();
        return new MomentNotificationResponse(isRead, ids);
    }
}
