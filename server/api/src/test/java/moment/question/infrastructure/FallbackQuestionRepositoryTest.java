package moment.question.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import moment.config.TestTags;
import moment.question.domain.FallbackQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class FallbackQuestionRepositoryTest {

    @Autowired
    private FallbackQuestionRepository fallbackQuestionRepository;


    @BeforeEach
    void setUp() {
        FallbackQuestion fallbackQuestion1 = new FallbackQuestion("공통질문1");
        FallbackQuestion fallbackQuestion2 = new FallbackQuestion("공통질문2");
        FallbackQuestion fallbackQuestion3 = new FallbackQuestion("공통질문3");

        fallbackQuestionRepository.save(fallbackQuestion1);
        fallbackQuestionRepository.save(fallbackQuestion2);
        fallbackQuestionRepository.save(fallbackQuestion3);
    }

    @Test
    void 사용하지_않은_임시_질문을_찾는다() {
        // when & then
        Optional<FallbackQuestion> fallbackQuestion = fallbackQuestionRepository.findFirstByIsUsedFalse();

        // then
        assertAll(
                () -> assertThat(fallbackQuestion).isPresent(),
                () -> assertThat(fallbackQuestion.get().isUsed()).isFalse()
        );
    }

}