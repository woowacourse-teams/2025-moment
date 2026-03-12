package moment.question.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import moment.config.TestTags;
import moment.global.domain.QuestionType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class QuestionTest {

    @Test
    void 질문을_생성한다() {
        assertThatCode(() -> new Question(
                "오늘의 기분은 어떠세요",
                QuestionType.COMMON,
                LocalDate.now(),
                LocalDate.now().plusDays(7), null)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 질문이_없으면_예외가_발생한다(String emptyContent) {
        assertThatCode(() -> new Question(
                emptyContent,
                QuestionType.COMMON,
                LocalDate.now(),
                LocalDate.now().plusDays(7), null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 질문이_200자가_넘으면_예외가_발생한다() {
        // given
        String longContent = "a".repeat(201);

        // when & then
        assertThatCode(() -> new Question(
                longContent,
                QuestionType.COMMON,
                LocalDate.now(),
                LocalDate.now().plusDays(7), null)).isInstanceOf(IllegalArgumentException.class);
    }
}