package moment.notification.service.eventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.global.domain.TargetType;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.dto.event.MomentLikeEvent;
import moment.notification.domain.NotificationCommand;
import moment.notification.domain.NotificationType;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.service.facade.NotificationFacadeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationFacadeService notificationFacadeService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        log.info("CommentCreateEvent received: momentId={}, momenterId={}",
            event.momentId(), event.momenterId());

        notificationFacadeService.notify(new NotificationCommand(
                event.momenterId(), event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                null, PushNotificationMessage.REPLY_TO_MOMENT));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinRequestEvent(GroupJoinRequestEvent event) {
        log.info("GroupJoinRequestEvent received: groupId={}, applicant={}",
            event.groupId(), event.nickname());

        notificationFacadeService.notify(new NotificationCommand(
                event.ownerId(), event.groupId(),
                NotificationType.GROUP_JOIN_REQUEST, TargetType.GROUP,
                event.groupId(), PushNotificationMessage.GROUP_JOIN_REQUEST));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinApprovedEvent(GroupJoinApprovedEvent event) {
        log.info("GroupJoinApprovedEvent received: groupId={}, memberId={}",
            event.groupId(), event.memberId());

        notificationFacadeService.notify(new NotificationCommand(
                event.userId(), event.groupId(),
                NotificationType.GROUP_JOIN_APPROVED, TargetType.GROUP,
                event.groupId(), PushNotificationMessage.GROUP_JOIN_APPROVED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupKickedEvent(GroupKickedEvent event) {
        log.info("GroupKickedEvent received: groupId={}, userId={}",
            event.groupId(), event.kickedUserId());

        notificationFacadeService.notify(new NotificationCommand(
                event.kickedUserId(), event.groupId(),
                NotificationType.GROUP_KICKED, TargetType.GROUP,
                event.groupId(), PushNotificationMessage.GROUP_KICKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMomentLikeEvent(MomentLikeEvent event) {
        log.info("MomentLikeEvent received: momentId={}, liker={}",
            event.momentId(), event.likerNickname());

        notificationFacadeService.notify(new NotificationCommand(
                event.momentOwnerId(), event.momentId(),
                NotificationType.MOMENT_LIKED, TargetType.MOMENT,
                null, PushNotificationMessage.MOMENT_LIKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikeEvent(CommentLikeEvent event) {
        log.info("CommentLikeEvent received: commentId={}, liker={}",
            event.commentId(), event.likerNickname());

        notificationFacadeService.notify(new NotificationCommand(
                event.commentOwnerId(), event.commentId(),
                NotificationType.COMMENT_LIKED, TargetType.COMMENT,
                null, PushNotificationMessage.COMMENT_LIKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupCommentCreateEvent(GroupCommentCreateEvent event) {
        log.info("GroupCommentCreateEvent received: momentId={}, commenter={}",
            event.momentId(), event.commenterNickname());

        notificationFacadeService.notify(new NotificationCommand(
                event.momentOwnerId(), event.momentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
                event.groupId(), PushNotificationMessage.REPLY_TO_MOMENT));
    }
}
