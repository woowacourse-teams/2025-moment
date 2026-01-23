package moment.notification.service.eventHandler;

import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.global.domain.TargetType;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.dto.event.MomentLikeEvent;
import moment.notification.domain.NotificationType;
import moment.notification.domain.PushNotificationMessage;
import moment.notification.service.facade.NotificationFacadeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventHandler 테스트")
class NotificationEventHandlerTest {

    @Mock
    private NotificationFacadeService notificationFacadeService;

    @InjectMocks
    private NotificationEventHandler eventHandler;

    @Test
    @DisplayName("그룹 가입 신청 이벤트 시 알림을 발송한다")
    void 그룹_가입_신청_알림_발송() {
        // Given
        GroupJoinRequestEvent event = new GroupJoinRequestEvent(1L, 2L, 3L, "신청자닉네임");

        // When
        eventHandler.handleGroupJoinRequestEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(2L),  // ownerId
            eq(1L),  // groupId (targetId)
            eq(NotificationType.GROUP_JOIN_REQUEST),
            eq(TargetType.GROUP),
            eq(1L),  // groupId
            eq(PushNotificationMessage.GROUP_JOIN_REQUEST)
        );
    }

    @Test
    @DisplayName("그룹 가입 승인 이벤트 시 알림을 발송한다")
    void 그룹_가입_승인_알림_발송() {
        // Given
        GroupJoinApprovedEvent event = new GroupJoinApprovedEvent(1L, 3L, 4L);

        // When
        eventHandler.handleGroupJoinApprovedEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(3L),  // userId
            eq(1L),  // groupId (targetId)
            eq(NotificationType.GROUP_JOIN_APPROVED),
            eq(TargetType.GROUP),
            eq(1L),  // groupId
            eq(PushNotificationMessage.GROUP_JOIN_APPROVED)
        );
    }

    @Test
    @DisplayName("그룹 강퇴 이벤트 시 알림을 발송한다")
    void 그룹_강퇴_알림_발송() {
        // Given
        GroupKickedEvent event = new GroupKickedEvent(1L, 3L, 4L);

        // When
        eventHandler.handleGroupKickedEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(3L),  // kickedUserId
            eq(1L),  // groupId (targetId)
            eq(NotificationType.GROUP_KICKED),
            eq(TargetType.GROUP),
            eq(1L),  // groupId
            eq(PushNotificationMessage.GROUP_KICKED)
        );
    }

    @Test
    @DisplayName("모멘트 좋아요 이벤트 시 알림을 발송한다")
    void 모멘트_좋아요_알림_발송() {
        // Given
        MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임");

        // When
        eventHandler.handleMomentLikeEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(2L),  // momentOwnerId
            eq(1L),  // momentId
            eq(NotificationType.MOMENT_LIKED),
            eq(TargetType.MOMENT),
            eq(null),  // groupId
            eq(PushNotificationMessage.MOMENT_LIKED)
        );
    }

    @Test
    @DisplayName("코멘트 좋아요 이벤트 시 알림을 발송한다")
    void 코멘트_좋아요_알림_발송() {
        // Given
        CommentLikeEvent event = new CommentLikeEvent(1L, 2L, 3L, "좋아요닉네임");

        // When
        eventHandler.handleCommentLikeEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(2L),  // commentOwnerId
            eq(1L),  // commentId
            eq(NotificationType.COMMENT_LIKED),
            eq(TargetType.COMMENT),
            eq(null),  // groupId
            eq(PushNotificationMessage.COMMENT_LIKED)
        );
    }

    @Test
    @DisplayName("그룹 코멘트 생성 이벤트 시 알림을 발송한다")
    void 그룹_코멘트_알림_발송() {
        // Given
        GroupCommentCreateEvent event = new GroupCommentCreateEvent(
            1L, 2L, 3L, 4L, 5L, "코멘터닉네임"
        );

        // When
        eventHandler.handleGroupCommentCreateEvent(event);

        // Then
        verify(notificationFacadeService).createNotificationAndSendSseAndPush(
            eq(3L),  // momentOwnerId
            eq(2L),  // momentId (targetId)
            eq(NotificationType.NEW_COMMENT_ON_MOMENT),
            eq(TargetType.MOMENT),
            eq(1L),  // groupId
            eq(PushNotificationMessage.REPLY_TO_MOMENT)
        );
    }

    @Test
    @DisplayName("자기 글에 자기가 코멘트 시 알림 미발송")
    void 자기_글_코멘트_시_알림_미발송() {
        // Given
        GroupCommentCreateEvent event = new GroupCommentCreateEvent(
            1L, 2L, 3L, 4L, 3L, "닉네임"  // momentOwnerId == commenterId
        );

        // When
        eventHandler.handleGroupCommentCreateEvent(event);

        // Then
        verify(notificationFacadeService, never()).createNotificationAndSendSseAndPush(
            any(), any(), any(), any(), any(), any()
        );
    }
}
