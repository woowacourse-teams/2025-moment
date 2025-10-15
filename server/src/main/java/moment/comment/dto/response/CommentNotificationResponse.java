package moment.comment.dto.response;

import java.util.List;

public record CommentNotificationResponse(
        boolean isRead, 
        List<Long> notificationIds
) {
    
    public static CommentNotificationResponse from(List<Long> unreadNotificationIds) {
        boolean isRead = unreadNotificationIds.isEmpty();
        return new CommentNotificationResponse(isRead, unreadNotificationIds);
    }
}
