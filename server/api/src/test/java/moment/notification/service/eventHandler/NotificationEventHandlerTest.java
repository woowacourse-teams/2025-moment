package moment.notification.service.eventHandler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;
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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationFacadeService notificationFacadeService;

    @Mock
    private UserBlockApplicationService userBlockApplicationService;

    @InjectMocks
    private NotificationEventHandler eventHandler;

    @Test
    void 코멘트_생성_이벤트_시_알림을_발송한다() {
        // given
        CommentCreateEvent event = new CommentCreateEvent(1L, 2L, 3L, 10L);
        given(userBlockApplicationService.isBlocked(3L, 2L)).willReturn(false);

        // when
        eventHandler.handleCommentCreateEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                2L,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L, "groupId", 10L)),
                PushNotificationMessage.REPLY_TO_MOMENT));
    }

    @Test
    void groupId가_없는_코멘트_생성_이벤트_시_알림을_발송한다() {
        // given
        CommentCreateEvent event = new CommentCreateEvent(1L, 2L, 3L, null);
        given(userBlockApplicationService.isBlocked(3L, 2L)).willReturn(false);

        // when
        eventHandler.handleCommentCreateEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                2L,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 1L)),
                PushNotificationMessage.REPLY_TO_MOMENT));
    }

    @Test
    void 그룹_가입_신청_이벤트_시_알림을_발송한다() {
        // given
        GroupJoinRequestEvent event = new GroupJoinRequestEvent(1L, 2L, 3L, "신청자닉네임");

        // when
        eventHandler.handleGroupJoinRequestEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                2L,
                NotificationType.GROUP_JOIN_REQUEST,
                SourceData.of(Map.of("groupId", 1L)),
                PushNotificationMessage.GROUP_JOIN_REQUEST));
    }

    @Test
    void 그룹_가입_승인_이벤트_시_알림을_발송한다() {
        // given
        GroupJoinApprovedEvent event = new GroupJoinApprovedEvent(1L, 3L, 4L);

        // when
        eventHandler.handleGroupJoinApprovedEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                3L,
                NotificationType.GROUP_JOIN_APPROVED,
                SourceData.of(Map.of("groupId", 1L)),
                PushNotificationMessage.GROUP_JOIN_APPROVED));
    }

    @Test
    void 그룹_강퇴_이벤트_시_알림을_발송한다() {
        // given
        GroupKickedEvent event = new GroupKickedEvent(1L, 3L, 4L);

        // when
        eventHandler.handleGroupKickedEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                3L,
                NotificationType.GROUP_KICKED,
                SourceData.of(Map.of("groupId", 1L)),
                PushNotificationMessage.GROUP_KICKED));
    }

    @Test
    void 모멘트_좋아요_이벤트_시_알림을_발송한다() {
        // given
        MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L, 100L);
        given(userBlockApplicationService.isBlocked(100L, 2L)).willReturn(false);

        // when
        eventHandler.handleMomentLikeEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                2L,
                NotificationType.MOMENT_LIKED,
                SourceData.of(Map.of("momentId", 1L, "groupId", 10L)),
                PushNotificationMessage.MOMENT_LIKED));
    }

    @Test
    void 코멘트_좋아요_이벤트_시_알림을_발송한다() {
        // given
        CommentLikeEvent event = new CommentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L, 100L);
        given(userBlockApplicationService.isBlocked(100L, 2L)).willReturn(false);

        // when
        eventHandler.handleCommentLikeEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                2L,
                NotificationType.COMMENT_LIKED,
                SourceData.of(Map.of("commentId", 1L, "groupId", 10L)),
                PushNotificationMessage.COMMENT_LIKED));
    }

    @Test
    void 그룹_코멘트_생성_이벤트_시_알림을_발송한다() {
        // given
        GroupCommentCreateEvent event = new GroupCommentCreateEvent(
                1L, 2L, 3L, 4L, 5L, "코멘터닉네임");
        given(userBlockApplicationService.isBlocked(5L, 3L)).willReturn(false);

        // when
        eventHandler.handleGroupCommentCreateEvent(event);

        // then
        verify(notificationFacadeService).notify(new NotificationCommand(
                3L,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                SourceData.of(Map.of("momentId", 2L, "groupId", 1L)),
                PushNotificationMessage.REPLY_TO_MOMENT));
    }

    @Test
    void 댓글_이벤트_차단된_사용자면_알림을_보내지_않는다() {
        // given
        CommentCreateEvent event = new CommentCreateEvent(1L, 2L, 3L, 10L);
        given(userBlockApplicationService.isBlocked(3L, 2L)).willReturn(true);

        // when
        eventHandler.handleCommentCreateEvent(event);

        // then
        verify(notificationFacadeService, never()).notify(any());
    }

    @Test
    void 그룹_댓글_이벤트_차단된_사용자면_알림을_보내지_않는다() {
        // given
        GroupCommentCreateEvent event = new GroupCommentCreateEvent(
                1L, 2L, 3L, 4L, 5L, "코멘터닉네임");
        given(userBlockApplicationService.isBlocked(5L, 3L)).willReturn(true);

        // when
        eventHandler.handleGroupCommentCreateEvent(event);

        // then
        verify(notificationFacadeService, never()).notify(any());
    }

    @Test
    void 모멘트_좋아요_이벤트_차단된_사용자면_알림을_보내지_않는다() {
        // given
        MomentLikeEvent event = new MomentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L, 100L);
        given(userBlockApplicationService.isBlocked(100L, 2L)).willReturn(true);

        // when
        eventHandler.handleMomentLikeEvent(event);

        // then
        verify(notificationFacadeService, never()).notify(any());
    }

    @Test
    void 댓글_좋아요_이벤트_차단된_사용자면_알림을_보내지_않는다() {
        // given
        CommentLikeEvent event = new CommentLikeEvent(1L, 2L, 3L, "좋아요닉네임", 10L, 100L);
        given(userBlockApplicationService.isBlocked(100L, 2L)).willReturn(true);

        // when
        eventHandler.handleCommentLikeEvent(event);

        // then
        verify(notificationFacadeService, never()).notify(any());
    }

    @Test
    void self_notification은_차단_확인_전에_스킵된다() {
        // given - commenterId == momenterId (self notification)
        CommentCreateEvent event = new CommentCreateEvent(1L, 2L, 2L, 10L);

        // when
        eventHandler.handleCommentCreateEvent(event);

        // then - 차단 서비스 호출 없이 알림도 보내지 않음
        verify(userBlockApplicationService, never()).isBlocked(any(), any());
        verify(notificationFacadeService, never()).notify(any());
    }
}
