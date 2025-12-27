package moment.notification.service.eventHandler;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.EchoCreateEvent;
import moment.global.domain.TargetType;
import moment.notification.domain.NotificationType;
import moment.notification.service.facade.NotificationFacadeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationFacadeService notificationFacadeService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        notificationFacadeService.createNotificationAndSendSse(
                event.momenterId(),
                event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEchoCreateEvent(EchoCreateEvent event) {
        notificationFacadeService.createNotificationAndSendSse(
                event.commenterId(),
                event.commentId(),
                NotificationType.NEW_REPLY_ON_COMMENT,
                TargetType.COMMENT);
    }
}
