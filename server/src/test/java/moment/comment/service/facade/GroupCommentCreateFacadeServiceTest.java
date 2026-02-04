package moment.comment.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupCommentCreateFacadeServiceTest {

    @Mock
    private CommentApplicationService commentApplicationService;

    @Mock
    private MomentApplicationService momentApplicationService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private GroupCommentCreateFacadeService groupCommentCreateFacadeService;

    @Test
    void 그룹_댓글_생성_시_GroupCommentCreateEvent를_발행한다() {
        // given
        Long groupId = 1L;
        Long momentId = 2L;
        Long userId = 5L;
        Long momentOwnerId = 3L;

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        GroupCommentResponse response = new GroupCommentResponse(
                10L, "댓글 내용", "작성자닉네임", 1L, 0L, false, null, LocalDateTime.now());

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(commentApplicationService.createCommentInGroup(
                groupId, momentId, userId, "댓글 내용", null, null))
                .thenReturn(response);

        // when
        groupCommentCreateFacadeService.createGroupComment(
                groupId, momentId, userId, "댓글 내용", null, null);

        // then
        ArgumentCaptor<GroupCommentCreateEvent> captor = ArgumentCaptor.forClass(GroupCommentCreateEvent.class);
        verify(publisher).publishEvent(captor.capture());

        GroupCommentCreateEvent event = captor.getValue();
        assertThat(event.groupId()).isEqualTo(groupId);
        assertThat(event.momentId()).isEqualTo(momentId);
        assertThat(event.momentOwnerId()).isEqualTo(momentOwnerId);
        assertThat(event.commentId()).isEqualTo(10L);
        assertThat(event.commenterId()).isEqualTo(userId);
    }

    @Test
    void 자기_모멘트에_댓글_작성_시_이벤트를_발행하지_않는다() {
        // given
        Long groupId = 1L;
        Long momentId = 2L;
        Long userId = 3L;
        Long momentOwnerId = 3L; // same as userId

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        GroupCommentResponse response = new GroupCommentResponse(
                10L, "댓글 내용", "작성자닉네임", 1L, 0L, false, null, LocalDateTime.now());

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(commentApplicationService.createCommentInGroup(
                groupId, momentId, userId, "댓글 내용", null, null))
                .thenReturn(response);

        // when
        groupCommentCreateFacadeService.createGroupComment(
                groupId, momentId, userId, "댓글 내용", null, null);

        // then
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void 댓글_생성_응답을_반환한다() {
        // given
        Long groupId = 1L;
        Long momentId = 2L;
        Long userId = 5L;
        Long momentOwnerId = 3L;

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        GroupCommentResponse expectedResponse = new GroupCommentResponse(
                10L, "댓글 내용", "작성자닉네임", 1L, 0L, false, null, LocalDateTime.now());

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(commentApplicationService.createCommentInGroup(
                groupId, momentId, userId, "댓글 내용", null, null))
                .thenReturn(expectedResponse);

        // when
        GroupCommentResponse result = groupCommentCreateFacadeService.createGroupComment(
                groupId, momentId, userId, "댓글 내용", null, null);

        // then
        assertThat(result).isEqualTo(expectedResponse);
    }

    private Moment createMomentWithOwner(Long momentId, Long ownerId) {
        User owner = UserFixture.createUser();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        Moment moment = new Moment("테스트 모멘트", owner);
        ReflectionTestUtils.setField(moment, "id", momentId);
        return moment;
    }
}
