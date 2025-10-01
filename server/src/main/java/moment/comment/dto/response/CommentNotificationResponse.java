package moment.comment.dto.response;

import java.util.List;
import moment.notification.domain.Notification;

public record CommentNotificationResponse(
        boolean isRead,
        List<Long> notificationIds
) {

    public static CommentNotificationResponse of(List<Notification> notifications) {
        boolean isRead = notifications.isEmpty();
        List<Long> ids = notifications.stream()
                .map(Notification::getId)
                .toList();
        return new CommentNotificationResponse(isRead, ids);
    }
}
