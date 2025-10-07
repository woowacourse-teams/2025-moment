package moment.notification.service.tobe.application;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.service.tobe.NotificationService;
import moment.notification.service.tobe.SseNotificationService;
import moment.user.application.tobe.user.UserService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationApplicationService {
    
    private final SseNotificationService sseNotificationService;
    private final NotificationService notificationService;
    private final UserService userService;
    
    public void createNotificationAndSendSse(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType
    ) {
        User user = userService.getUserById(userId);

        Notification savedNotification = notificationService.saveNotification(
                user,
                targetId,
                notificationType,
                targetType);

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                savedNotification.getId(),
                notificationType,
                targetType,
                targetId);

        sseNotificationService.sendToClient(user.getId(), "notification", response);
    }
}
