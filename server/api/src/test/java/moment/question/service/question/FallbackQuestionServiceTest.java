package moment.question.service.question;

import static org.assertj.core.api.Assertions.assertThat;

import moment.config.TestTags;
import moment.question.domain.FallbackQuestion;
import moment.question.infrastructure.FallbackQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class FallbackQuestionServiceTest {

    @Autowired
    private FallbackQuestionRepository fallbackQuestionRepository;

    @Autowired
    private FallbackQuestionService fallbackQuestionService;

    @BeforeEach
    void setUp() {
        fallbackQuestionRepository.save(new FallbackQuestion("임시질문입니다."));
    }

    @Test
    void 사용하지_않은_임시_질문을_불러온다() {
        // given
        FallbackQuestion fallbackQuestion = fallbackQuestionService.getUnusedFallbackContent();

        // when && then
        assertThat(fallbackQuestion.isUsed()).isFalse();
    }

    @Test
    void 임시_질문을_사용_처리한다() {
        // given
        FallbackQuestion fallbackQuestion = fallbackQuestionRepository.save(new FallbackQuestion("임시 질문1"));

        // when
        fallbackQuestionService.markAsUsed(fallbackQuestion.getId());

        // then
        assertThat(fallbackQuestion.isUsed()).isTrue();
    }
}
