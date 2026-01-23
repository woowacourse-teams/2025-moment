package moment.notification.service.facade;

import lombok.RequiredArgsConstructor;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.service.application.NotificationApplicationService;
import moment.notification.service.application.PushNotificationApplicationService;
import moment.notification.service.notification.SseNotificationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationFacadeService {

    private final NotificationApplicationService notificationApplicationService;
    private final SseNotificationService sseNotificationService;
    private final PushNotificationApplicationService pushNotificationApplicationService;

    public void createNotificationAndSendSse(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType
    ) {
        Notification savedNotification = notificationApplicationService.createNotification(
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

    public void createNotificationAndSendSseAndSendToDeviceEndpoint(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType,
            PushNotificationMessage message
    ) {
        createNotificationAndSendSse(userId, targetId, notificationType, targetType);
        pushNotificationApplicationService.sendToDeviceEndpoint(userId, message);
    }

    public void createNotificationWithGroupIdAndSendSse(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType,
            Long groupId
    ) {
        Notification savedNotification = notificationApplicationService.createNotificationWithGroupId(
                userId,
                targetId,
                notificationType,
                targetType,
                groupId);

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                savedNotification.getId(),
                notificationType,
                targetType,
                targetId);

        sseNotificationService.sendToClient(userId, "notification", response);
    }

    public void createNotificationWithGroupIdAndSendSseAndSendPush(
            Long userId,
            Long targetId,
            NotificationType notificationType,
            TargetType targetType,
            Long groupId,
            PushNotificationMessage message
    ) {
        createNotificationWithGroupIdAndSendSse(userId, targetId, notificationType, targetType, groupId);
        pushNotificationApplicationService.sendToDeviceEndpoint(userId, message);
    }
}
