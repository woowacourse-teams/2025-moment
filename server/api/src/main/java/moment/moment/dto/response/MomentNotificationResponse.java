package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "모멘트 알림 정보")
public record MomentNotificationResponse(
        @Schema(description = "알림 읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "알림 ID 목록")
        List<Long> notificationIds
) {

    public static MomentNotificationResponse from(List<Long> unreadNotificationIds) {
        boolean isRead = unreadNotificationIds.isEmpty();
        return new MomentNotificationResponse(isRead, unreadNotificationIds);
    }
}
