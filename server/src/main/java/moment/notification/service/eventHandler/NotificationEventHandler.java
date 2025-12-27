package moment.notification.service.eventHandler;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.EchoCreateEvent;
import moment.global.domain.TargetType;
import moment.notification.domain.NotificationType;
import moment.notification.service.application.NotificationApplicationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationApplicationService notificationApplicationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        notificationApplicationService.createNotificationAndSendSse(
                event.momenterId(),
                event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEchoCreateEvent(EchoCreateEvent event) {
        notificationApplicationService.createNotificationAndSendSse(
                event.commenterId(),
                event.commentId(),
                NotificationType.NEW_REPLY_ON_COMMENT,
                TargetType.COMMENT);
    }
}
