package moment.comment.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
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
class DefaultMomentQueryServiceTest {

    @InjectMocks
    private DefaultCommentQueryService defaultCommentQueryService;

    @Mock
    private CommentRepository commentRepository;

    @Test
    void Comment_ID를_이용하여_조회한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(commentRepository.findById(any(Long.class))).willReturn(Optional.of(comment));

        // when & then
        assertAll(
                () -> assertThatCode(() -> defaultCommentQueryService.getCommentById(1L)).doesNotThrowAnyException(),
                () -> assertThat(defaultCommentQueryService.getCommentById(1L)).isInstanceOf(Comment.class)
        );
    }

    @Test
    void Comment_ID와_일치하는_Comment가_존재하지_않으면_예외가_발생한다() {
        // given
        given(commentRepository.findById(any(Long.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> defaultCommentQueryService.getCommentById(1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void Momment에_등록된_Comment가_존재하면_true를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);

        given(commentRepository.existsByMoment(any(Moment.class))).willReturn(true);

        // when & then
        assertThat(defaultCommentQueryService.existsByMoment(moment)).isTrue();
    }

    @Test
    void Momment에_등록된_Comment가_존재하면_false를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);

        given(commentRepository.existsByMoment(any(Moment.class))).willReturn(false);

        // when & then
        assertThat(defaultCommentQueryService.existsByMoment(moment)).isFalse();
    }
}
