package moment.notification.service.application;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.NotificationType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.service.notification.SseNotificationService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SseNotificationApplicationService {

    private final SseNotificationService sseNotificationService;
    private final UserService userService;

    public void sendSse(
            Long userId,
            Long notificationId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType) {

        User user = userService.getUserBy(userId);
        
        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                notificationId,
                notificationType,
                targetType,
                targetId);

        sseNotificationService.sendToClient(user.getId(), "notification", response);
    }
}
