package moment.notification.application;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacade {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;

    @Transactional
    public void sendSseNotificationAndNotification(User momenter,
                                                   Long momentId,
                                                   NotificationType notificationType,
                                                   TargetType targetType) {
        Notification savedNotification = notificationService.saveNotification(
                momenter,
                momentId,
                notificationType,
                targetType);

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                savedNotification.getId(),
                notificationType,
                targetType,
                momentId);

        sseNotificationService.sendToClient(momenter.getId(), "notification", response);
    }
}
