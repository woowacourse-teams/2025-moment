package moment.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;

@Schema(description = "SSE 알림 응답")
public record NotificationSseResponse(
        @Schema(description = "알림 id", example = "1")
        Long notificationId,

        @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
        NotificationType notificationType,

        @Schema(description = "메시지", example = "내 모멘트에 새로운 코멘트가 달렸습니다.")
        String message,

        @Schema(description = "딥링크", example = "/moments/1")
        String link
) {

    public static NotificationSseResponse from(Notification notification) {
        return new NotificationSseResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getNotificationType().getMessage(),
                notification.getLink());
    }
}
