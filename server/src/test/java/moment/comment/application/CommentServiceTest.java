package moment.comment.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Test
    void Comment를_등록한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userRepository.findById(any(Long.class))).willReturn(
                Optional.of(new User("hippo@gmail.com", "1234", "hippo")));
        given(momentRepository.findById(any(Long.class))).willReturn(
                Optional.of(new Moment("오늘 하루는 힘든 하루~", true, new User("kiki@icloud.com", "1234", "kiki"))));

        // when
        commentService.addComment(request, 1L);

        // then
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    void 존재하지_않는_Moment에_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userRepository.findById(any(Long.class))).willReturn(
                Optional.of(new User("hippo@gmail.com", "1234", "hippo")));
        given(momentRepository.findById(any(Long.class))).willReturn(
                Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_User가_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userRepository.findById(any(Long.class))).willReturn(
                Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
