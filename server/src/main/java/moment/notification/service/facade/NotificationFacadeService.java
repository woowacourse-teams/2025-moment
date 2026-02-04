package moment.notification.service.facade;

import lombok.RequiredArgsConstructor;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationCommand;
import moment.notification.domain.NotificationPayload;
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

    public void notify(NotificationCommand command) {
        Notification savedNotification = notificationApplicationService.createNotification(
                command.userId(), command.targetId(), command.notificationType(),
                command.targetType(), command.groupId());

        NotificationPayload payload = NotificationPayload.from(savedNotification);

        sseNotificationService.sendToClient(command.userId(), "notification",
                NotificationSseResponse.of(payload));

        if (command.pushMessage() != null) {
            pushNotificationApplicationService.sendToDeviceEndpoint(
                    command.userId(), command.pushMessage());
        }
    }
}
