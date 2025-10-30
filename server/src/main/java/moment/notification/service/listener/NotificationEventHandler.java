package moment.notification.service.listener;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.event.CommentCreatedEvent;
import moment.global.domain.TargetType;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.service.application.NotificationApplicationService;
import moment.notification.service.application.PushNotificationApplicationService;
import moment.notification.service.application.SseNotificationApplicationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationApplicationService notificationApplicationService;
    private final PushNotificationApplicationService pushNotificationApplicationService;
    private final SseNotificationApplicationService sseNotificationApplicationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        Notification notification = notificationApplicationService.createNotification(
                event.momenterId(),
                event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT
        );

        sseNotificationApplicationService.sendSse(
                event.momenterId(),
                notification.getId(),
                event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT
        );

        pushNotificationApplicationService.sendToDeviceEndpoint(
                event.momenterId(),
                PushNotificationMessage.REPLY_TO_MOMENT
        );
    }
}
