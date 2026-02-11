package moment.comment.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import moment.block.service.application.UserBlockApplicationService;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentCreateFacadeServiceTest {

    @Mock
    private CommentApplicationService commentApplicationService;

    @Mock
    private MomentApplicationService momentApplicationService;

    @Mock
    private UserBlockApplicationService userBlockApplicationService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private CommentCreateFacadeService commentCreateFacadeService;

    @Test
    void 차단된_사용자의_모멘트에_댓글_작성_시_예외가_발생한다() {
        // given
        Long userId = 5L;
        Long momentOwnerId = 3L;
        Long momentId = 2L;

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용", momentId, null, null);

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(userBlockApplicationService.isBlocked(userId, momentOwnerId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> commentCreateFacadeService.createComment(request, userId))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCKED_USER_INTERACTION);

        verify(commentApplicationService, never()).createComment(any(), any());
    }

    @Test
    void 차단되지_않은_경우_정상_댓글_생성() {
        // given
        Long userId = 5L;
        Long momentOwnerId = 3L;
        Long momentId = 2L;

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용", momentId, null, null);
        CommentCreateResponse expectedResponse = new CommentCreateResponse(
                10L, "댓글 내용", LocalDateTime.now(), null);

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(userBlockApplicationService.isBlocked(userId, momentOwnerId)).thenReturn(false);
        when(commentApplicationService.createComment(request, userId)).thenReturn(expectedResponse);

        // when
        CommentCreateResponse result = commentCreateFacadeService.createComment(request, userId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(publisher).publishEvent(any(CommentCreateEvent.class));
    }

    @Test
    void 자기_모멘트에_댓글_작성_시_이벤트를_발행하지_않는다() {
        // given
        Long userId = 3L;
        Long momentOwnerId = 3L;
        Long momentId = 2L;

        Moment moment = createMomentWithOwner(momentId, momentOwnerId);
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용", momentId, null, null);
        CommentCreateResponse expectedResponse = new CommentCreateResponse(
                10L, "댓글 내용", LocalDateTime.now(), null);

        when(momentApplicationService.getMomentBy(momentId)).thenReturn(moment);
        when(userBlockApplicationService.isBlocked(userId, momentOwnerId)).thenReturn(false);
        when(commentApplicationService.createComment(request, userId)).thenReturn(expectedResponse);

        // when
        commentCreateFacadeService.createComment(request, userId);

        // then
        verify(publisher, never()).publishEvent(any());
    }

    private Moment createMomentWithOwner(Long momentId, Long ownerId) {
        User owner = UserFixture.createUser();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        Moment moment = new Moment("테스트 모멘트", owner);
        ReflectionTestUtils.setField(moment, "id", momentId);
        return moment;
    }
}
