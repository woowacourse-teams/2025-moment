package moment.notification.service.facade;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.service.application.NotificationApplicationService;
import moment.notification.service.notification.SseNotificationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationFacadeService {

    private final NotificationApplicationService notificationApplicationService;
    private final SseNotificationService sseNotificationService;

    public void createNotificationAndSendSse(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType
    ) {
        Notification savedNotification = notificationApplicationService.createNotificationWithNewTransaction(
                userId,
                targetId,
                notificationType,
                targetType);

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                savedNotification.getId(),
                notificationType,
                targetType,
                targetId);

        sseNotificationService.sendToClient(userId, "notification", response);
    }
}
