package moment.notification.service.eventHandler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.block.service.application.UserBlockApplicationService;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.dto.event.MomentLikeEvent;
import moment.notification.domain.NotificationCommand;
import moment.notification.domain.NotificationType;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.domain.SourceData;
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
    private final UserBlockApplicationService userBlockApplicationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        log.info("CommentCreateEvent received: momentId={}, momenterId={}",
            event.momentId(), event.momenterId());

        if (event.commenterId().equals(event.momenterId())) {
            return;
        }
        if (userBlockApplicationService.isBlocked(event.commenterId(), event.momenterId())) {
            log.info("Skipping notification due to block: commenter={}, momentOwner={}",
                event.commenterId(), event.momenterId());
            return;
        }

        SourceData sourceData = event.groupId() != null
                ? SourceData.of(Map.of("momentId", event.momentId(), "groupId", event.groupId()))
                : SourceData.of(Map.of("momentId", event.momentId()));

        notificationFacadeService.notify(new NotificationCommand(
                event.momenterId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                sourceData,
                PushNotificationMessage.REPLY_TO_MOMENT));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinRequestEvent(GroupJoinRequestEvent event) {
        log.info("GroupJoinRequestEvent received: groupId={}, applicant={}",
            event.groupId(), event.nickname());

        notificationFacadeService.notify(new NotificationCommand(
                event.ownerId(),
                NotificationType.GROUP_JOIN_REQUEST,
                SourceData.of(Map.of("groupId", event.groupId())),
                PushNotificationMessage.GROUP_JOIN_REQUEST));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupJoinApprovedEvent(GroupJoinApprovedEvent event) {
        log.info("GroupJoinApprovedEvent received: groupId={}, memberId={}",
            event.groupId(), event.memberId());

        notificationFacadeService.notify(new NotificationCommand(
                event.userId(),
                NotificationType.GROUP_JOIN_APPROVED,
                SourceData.of(Map.of("groupId", event.groupId())),
                PushNotificationMessage.GROUP_JOIN_APPROVED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupKickedEvent(GroupKickedEvent event) {
        log.info("GroupKickedEvent received: groupId={}, userId={}",
            event.groupId(), event.kickedUserId());

        notificationFacadeService.notify(new NotificationCommand(
                event.kickedUserId(),
                NotificationType.GROUP_KICKED,
                SourceData.of(Map.of("groupId", event.groupId())),
                PushNotificationMessage.GROUP_KICKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMomentLikeEvent(MomentLikeEvent event) {
        log.info("MomentLikeEvent received: momentId={}, liker={}",
            event.momentId(), event.likerNickname());

        if (event.likerUserId().equals(event.momentOwnerId())) {
            return;
        }
        if (userBlockApplicationService.isBlocked(event.likerUserId(), event.momentOwnerId())) {
            log.info("Skipping notification due to block: liker={}, momentOwner={}",
                event.likerUserId(), event.momentOwnerId());
            return;
        }

        notificationFacadeService.notify(new NotificationCommand(
                event.momentOwnerId(),
                NotificationType.MOMENT_LIKED,
                SourceData.of(Map.of("momentId", event.momentId(), "groupId", event.groupId())),
                PushNotificationMessage.MOMENT_LIKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikeEvent(CommentLikeEvent event) {
        log.info("CommentLikeEvent received: commentId={}, liker={}",
            event.commentId(), event.likerNickname());

        if (event.likerUserId().equals(event.commentOwnerId())) {
            return;
        }
        if (userBlockApplicationService.isBlocked(event.likerUserId(), event.commentOwnerId())) {
            log.info("Skipping notification due to block: liker={}, commentOwner={}",
                event.likerUserId(), event.commentOwnerId());
            return;
        }

        notificationFacadeService.notify(new NotificationCommand(
                event.commentOwnerId(),
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", event.commentId(), "groupId", event.groupId())),
                PushNotificationMessage.COMMENT_LIKED));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupCommentCreateEvent(GroupCommentCreateEvent event) {
        log.info("GroupCommentCreateEvent received: momentId={}, commenter={}",
            event.momentId(), event.commenterNickname());

        if (event.commenterId().equals(event.momentOwnerId())) {
            return;
        }
        if (userBlockApplicationService.isBlocked(event.commenterId(), event.momentOwnerId())) {
            log.info("Skipping notification due to block: commenter={}, momentOwner={}",
                event.commenterId(), event.momentOwnerId());
            return;
        }

        notificationFacadeService.notify(new NotificationCommand(
                event.momentOwnerId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of(
                        "momentId", event.momentId(),
                        "groupId", event.groupId())),
                PushNotificationMessage.REPLY_TO_MOMENT));
    }
}
