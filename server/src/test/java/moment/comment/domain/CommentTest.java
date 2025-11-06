package moment.comment.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.config.TestTags;
import moment.fixture.UserFixture;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentTest {

    @Test
    void Comment를_생성한다() {
        assertThatCode(() -> {
                    new Comment(
                            "정말 안타깝게 됐네요!",
                            UserFixture.createUser(),
                            1L
                    );
                }
        ).doesNotThrowAnyException();
    }

    @Test
    void Comment가_200자를_넘으면_예외가_발생한다() {
        // given
        String longContent = "a".repeat(201);

        // when & then
        assertThatThrownBy(() -> new Comment(
                longContent,
                UserFixture.createUser(),
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content가 200자를 초과해서는 안 됩니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void Comment가_빈_값인_경우_예외가_발생한다(String content) {
        assertThatThrownBy(() -> new Comment(
                content,
                UserFixture.createUser(),
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content가 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void Comment_생성_시_Moment가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                UserFixture.createUser(),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("momentId가 null이어서는 안 됩니다.");
    }

    @Test
    void Comment_생성_시_Commenter가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Comment(
                "정말 안타깝게 됐네요!",
                null,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commenter가 null이어서는 안 됩니다.");
    }
}
