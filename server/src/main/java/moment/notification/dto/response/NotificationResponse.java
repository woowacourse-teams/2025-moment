package moment.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.global.domain.TargetType;

@Schema(description = "알림 응답")
public record NotificationResponse(
        @Schema(description = "알림 id", example = "1")
        Long id,

        @Schema(description = "알림 타입", example = "NEW_COMMENT_ON_MOMENT")
        NotificationType notificationType,

        @Schema(description = "타겟 타입", example = "MOMENT")
        TargetType targetType,

        @Schema(description = "타겟 id", example = "1")
        Long targetId,

        @Schema(description = "그룹 id", example = "1")
        Long groupId,

        @Schema(description = "메시지", example = "알림이 전송되었습니다.")
        String message,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead
) {

    public static NotificationResponse from(Notification notification) {
        NotificationType notificationType = notification.getNotificationType();
        return new NotificationResponse(
                notification.getId(),
                notificationType,
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getGroupId(),
                notificationType.getMessage(),
                notification.isRead()
        );
    }
}
