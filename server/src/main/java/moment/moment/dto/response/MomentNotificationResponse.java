package moment.moment.dto.response;

import java.util.List;

public record MomentNotificationResponse(
        boolean isRead,
        List<Long> notificationIds
) {

    public static MomentNotificationResponse from(List<Long> unreadNotificationIds) {
        boolean isRead = unreadNotificationIds.isEmpty();
        return new MomentNotificationResponse(isRead, unreadNotificationIds);
    }
}
