package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "코멘트 알림 정보")
public record CommentNotificationResponse(
        @Schema(description = "알림 읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "알림 ID 목록")
        List<Long> notificationIds
) {
    
    public static CommentNotificationResponse from(List<Long> unreadNotificationIds) {
        boolean isRead = unreadNotificationIds.isEmpty();
        return new CommentNotificationResponse(isRead, unreadNotificationIds);
    }
}
