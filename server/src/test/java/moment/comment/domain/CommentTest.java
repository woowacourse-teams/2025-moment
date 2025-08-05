package moment.comment.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentTest {

    @Test
    void Comment를_생성한다() {
        assertThatCode(() -> {
                    new Comment(
                            "정말 안타깝게 됐네요!",
                            new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL),
                            new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                                    true,
                                    new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL)
                            )
                    );
                }
        ).doesNotThrowAnyException();
    }

    @Test
    void Comment가_100자를_넘으면_예외가_발생한다() {
        // given
        String longContent = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> new Comment(
                longContent,
                new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL),
                new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                        true,
                        new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL)
                )
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content가 100자를 초과해서는 안 됩니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void Comment가_빈_값인_경우_예외가_발생한다(String content) {
        assertThatThrownBy(() -> new Comment(
                content,
                new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL),
                new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                        true,
                        new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL)
                )
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content가 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void Comment_생성_시_Moment가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("moment가 null이어서는 안 됩니다.");
    }

    @Test
    void Comment_생성_시_Commenter가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                null,
                new Moment("오늘도 야근 예정이에요", new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commenter가 null이어서는 안 됩니다.");
    }

    @Test
    void Comment와_Moment_작성자가_아니라면_예외가_발생한다() {
        // given
        User momenter = new User("momenter@email.com", "1234", "momter", ProviderType.EMAIL);
        ReflectionTestUtils.setField(momenter, "id", 1L);

        User commenter = new User("commenter@email.com", "1234", "comter", ProviderType.EMAIL);
        ReflectionTestUtils.setField(commenter, "id", 2L);

        User unAuthorized = new User("no@email.com", "1", "nouser", ProviderType.EMAIL);
        ReflectionTestUtils.setField(unAuthorized, "id", 3L);

        Moment moment = new Moment("오늘 야근 정말 힘들었네요", momenter);
        Comment comment = new Comment("수고하셨습니다.!", commenter, moment);

        // when & then
        assertThatThrownBy(() -> comment.checkAuthorization(unAuthorized))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }
}
