package moment.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentCreationStatus;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreationStatusResponse;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
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

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
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

        given(userQueryService.getUserById(any(Long.class))).willReturn(new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL));
        given(momentQueryService.getMomentById(any(Long.class))).willThrow(new MomentException(ErrorCode.MOMENT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_User가_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        given(userQueryService.getUserById(any(Long.class))).willThrow(new MomentException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void Commenter_ID가_일치하는_Comment_목록을_생성_시간_내림차순으로_불러온다() {
        // given
        User momenter1 = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        User momenter2 = new User("drago@gmail.com", "1234", "drago", ProviderType.EMAIL);
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);

        Moment moment1 = new Moment("오늘 하루는 맛있는 하루~", true, momenter1);
        Moment moment2 = new Moment("오늘 하루는 행복한 하루~", true, momenter2);
        Comment comment1 = new Comment("moment1 comment", commenter, moment1);
        Comment comment2 = new Comment("moment2 comment", commenter, moment2);

        // given

        List<Comment> expectedComments = List.of(comment1, comment2);

        given(commentRepository.findCommentsByCommenterIdOrderByCreatedAtDesc(any(Long.class)))
                .willReturn(expectedComments);
        given(userQueryService.existsById(any(Long.class))).willReturn(true);
        given(emojiRepository.findAllByCommentIn(any(List.class))).willReturn(Collections.emptyList());

        // when
        List<MyCommentsResponse> actualComments = commentService.getCommentsByUserId(1L);

        // then
        assertAll(
                () -> assertThat(actualComments).hasSize(2),
                () -> then(commentRepository).should(times(1)).findCommentsByCommenterIdOrderByCreatedAtDesc(1L)
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

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
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

    @Test
    void 아직_매칭된_모멘트가_존재하지_않을_경우의_상태를_반환한다() {
        // given
        Long commenterId = 1L;
        User commenter = new User("mimi@icloud.com",  "1234", "mimi", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.findTodayMatchedMomentByCommenter(any(User.class))).willReturn(Optional.empty());

        // when
        CommentCreationStatusResponse response = commentService.canCreateComment(commenterId);

        // then
        assertAll(
                () -> assertThat(response.commentCreationStatus()).isEqualTo(CommentCreationStatus.NOT_MATCHED),
                () -> then(momentQueryService).should(times(1)).findTodayMatchedMomentByCommenter(commenter)
        );
    }

    @Test
    void 이미_매칭된_모멘트에_코멘트를_작성한_경우의_상태를_반환한다() {
        // given
        Long commenterId = 1L;
        User commenter = new User("mimi@icloud.com",  "1234", "mimi", ProviderType.EMAIL);
        User momenter = new User("hippo@icloud.com",  "1234", "hippo", ProviderType.EMAIL);
        Moment moment = new Moment("집가고 싶어요..", momenter);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.findTodayMatchedMomentByCommenter(any(User.class))).willReturn(Optional.of(moment));
        given(commentRepository.existsByMoment(any(Moment.class))).willReturn(true);

        // when
        CommentCreationStatusResponse response = commentService.canCreateComment(commenterId);

        // then
        assertAll(
                () -> assertThat(response.commentCreationStatus()).isEqualTo(CommentCreationStatus.ALREADY_COMMENTED),
                () -> then(commentRepository).should(times(1)).existsByMoment(moment)
        );
    }

    @Test
    void 코멘트를_등록할_수_있는_상태를_반환한다() {
        // given
        Long commenterId = 1L;
        User commenter = new User("mimi@icloud.com",  "1234", "mimi", ProviderType.EMAIL);
        User momenter = new User("hippo@icloud.com",  "1234", "hippo", ProviderType.EMAIL);
        Moment moment = new Moment("집가고 싶어요..", momenter);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.findTodayMatchedMomentByCommenter(any(User.class))).willReturn(Optional.of(moment));
        given(commentRepository.existsByMoment(any(Moment.class))).willReturn(false);

        // when
        CommentCreationStatusResponse response = commentService.canCreateComment(commenterId);

        // then
        assertAll(
                () -> assertThat(response.commentCreationStatus()).isEqualTo(CommentCreationStatus.WRITABLE),
                () -> then(commentRepository).should(times(1)).existsByMoment(moment)
        );
    }
}
