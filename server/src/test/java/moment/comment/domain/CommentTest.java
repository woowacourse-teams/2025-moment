package moment.comment.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentTest {

    @Test
    void Comment를_생성한다() {
        assertThatCode(() -> {
                    new Comment(
                            "정말 안타깝게 됐네요!",
                            new User("hippo@gmail.com", "1234", "hippo"),
                            new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                                    true,
                                    new User("kiki@icloud.com", "1234", "kiki")
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
                new User("hippo@gmail.com", "1234", "hippo"),
                new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                        true,
                        new User("kiki@icloud.com", "1234", "kiki")
                )
        )).isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void Comment가_빈_값인_경우_예외가_발생한다(String content) {
        assertThatThrownBy(() -> new Comment(
                content,
                new User("hippo@gmail.com", "1234", "hippo"),
                new Moment("오늘 면접에서 떨어졌어요...ㅜㅜ",
                        true,
                        new User("kiki@icloud.com", "1234", "kiki")
                )
        )).isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @Test
    void Comment_생성_시_Moment가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                new User("hippo@gmail.com", "1234", "hippo"),
                null
        )).isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @Test
    void Comment_생성_시_Commenter가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                new User("hippo@gmail.com", "1234", "hippo"),
                null
        )).isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }
}
