package moment.question.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import moment.config.TestTags;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class FallbackQuestionTest {

    @Test
    void 임시_질문을_생성한다() {
        assertThatCode(() -> new FallbackQuestion("오늘의 기분은 어떠세요")).doesNotThrowAnyException();
    }

    @Test
    void 임시_질문은_사용하지_않은_상태로_생성된다() {
        FallbackQuestion fallbackQuestion = new FallbackQuestion("오늘의 기분은 어떠세요");

        assertThat(fallbackQuestion.isUsed()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 질문이_없으면_예외가_발생한다(String emptyContent) {
        assertThatCode(() -> new FallbackQuestion(emptyContent)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 질문이_200자가_넘으면_예외가_발생한다() {
        // given
        String longContent = "a".repeat(201);

        // when & then
        assertThatCode(() -> new FallbackQuestion(longContent)).isInstanceOf(IllegalArgumentException.class);
    }
}
