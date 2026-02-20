package moment.notification.service.facade;

import lombok.RequiredArgsConstructor;
import moment.notification.domain.DeepLinkGenerator;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationCommand;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.service.application.NotificationApplicationService;
import moment.notification.service.application.PushNotificationApplicationService;
import moment.notification.service.notification.SseNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacadeService {

    private final NotificationApplicationService notificationApplicationService;
    private final SseNotificationService sseNotificationService;
    private final PushNotificationApplicationService pushNotificationApplicationService;

    public void notify(NotificationCommand command) {
        String link = DeepLinkGenerator.generate(
                command.notificationType(), command.sourceData());

        Notification savedNotification = notificationApplicationService.createNotification(
                command.userId(), command.notificationType(),
                command.sourceData(), link);

        NotificationSseResponse sseResponse = NotificationSseResponse.from(savedNotification);
        sseNotificationService.sendToClient(command.userId(), "notification", sseResponse);

        if (command.pushMessage() != null) {
            pushNotificationApplicationService.sendToDeviceEndpoint(
                    command.userId(), command.pushMessage(), link);
        }
    }
}
