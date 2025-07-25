package moment.comment.application;

import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private MomentQueryService momentQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private EmojiRepository emojiRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentQueryService commentQueryService;

    @Test
    void Comment를_등록한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        commentService.addComment(request, 1L);

        // then
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    void 존재하지_않는_Moment에_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(new User("hippo@gmail.com", "1234", "hippo"));
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @Test
    void 존재하지_않는_User가_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @Test
    void Commenter_ID가_일치하는_Comment_목록을_불러온다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        Comment comment = new Comment("첫 번째 댓글", commenter, moment);

        List<Comment> expectedComments = List.of(comment);
        given(commentRepository.findCommentsByCommenterId(any(Long.class)))
                .willReturn(expectedComments);
        given(userQueryService.existsById(any(Long.class))).willReturn(true);
        given(emojiRepository.findAllByCommentIn(any(List.class))).willReturn(Collections.emptyList());

        // when
        List<MyCommentsResponse> actualComments = commentService.getCommentsByUserId(1L);

        // then
        assertAll(
                () -> assertThat(actualComments).hasSize(1),
                () -> then(commentRepository).should(times(1)).findCommentsByCommenterId(1L)
        );
    }

    @Test
    void 존재하지_않는_Commenter가_Comment_목록을_조회하는_경우_예외가_발생한다() {
        // given
        given(userQueryService.existsById(any(Long.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByUserId(1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 코멘트가_이미_등록된_모멘트에_코멘트를_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentQueryService.existsByMoment(any(Moment.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_CONFLICT);
    }


}
