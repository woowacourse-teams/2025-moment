package moment.question.service.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import moment.config.TestTags;
import moment.global.domain.QuestionType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.Question;
import moment.question.infrastructure.QuestionRepository;
import moment.support.FakeTimeProvider;
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
class QuestionServiceTest {

    @Autowired
    private QuestionRepository questionRepository;
    private FakeTimeProvider fakeTimeProvider;
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        fakeTimeProvider = new FakeTimeProvider(LocalDateTime.now());
        questionService = new QuestionService(questionRepository);
    }

    @Test
    void 수요일에_질문을_조회하면_그주_월요일에_만들어진_공통_질문을_조회한다() {
        // given
        String content = "공통 질문입니다";
        LocalDate monday = LocalDate.of(2026, 3, 9);
        LocalDate sunday = monday.plusDays(6);
        Question savedQuestion = questionRepository.save(
                new Question(content, QuestionType.COMMON, monday, sunday, null)
        );

        fakeTimeProvider.changeTime(LocalDateTime.of(2026, 3, 11, 10, 0));

        // when
        Question result = questionService.findQuestion(null, QuestionType.COMMON, monday, sunday);

        // then
        assertThat(result.getId()).isEqualTo(savedQuestion.getId());
        assertThat(result.getContent()).isEqualTo(content);
    }

    @Test
    void 수요일에_질문을_조회하면_그주_월요일에_만들어진_커스텀_질문을_조회한다() {
        // given
        Long groupId = 1L;
        String content = "커스텀 질문입니다";
        LocalDate monday = LocalDate.of(2026, 3, 9);
        LocalDate sunday = monday.plusDays(6);
        Question savedQuestion = questionRepository.save(
                new Question(content, QuestionType.CUSTOM, monday, sunday, groupId)
        );

        fakeTimeProvider.changeTime(LocalDateTime.of(2026, 3, 11, 10, 0));

        // when
        Question result = questionService.findQuestion(groupId, QuestionType.CUSTOM, monday, sunday);

        // then
        assertThat(result.getId()).isEqualTo(savedQuestion.getId());
        assertThat(result.getContent()).isEqualTo(content);
    }

    @Test
    void 공통_질문이_없다면_예외가_발생한다() {
        // given
        String content = "공통 질문입니다";
        LocalDate monday = LocalDate.of(2026, 4, 9);
        LocalDate sunday = monday.plusDays(6);
        Question savedQuestion = questionRepository.save(
                new Question(content, QuestionType.COMMON, monday, sunday, null)
        );

        fakeTimeProvider.changeTime(LocalDateTime.of(2026, 3, 11, 10, 0));

        // when & then
        assertThatCode(() -> questionService.findQuestion(null, QuestionType.COMMON, monday, monday.plusDays(5)))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.QUESTION_NOT_FOUND.getMessage());
    }

    @Test
    void 커스텀_질문이_없다면_예외가_발생한다_날짜() {
        // given
        String content = "커스텀 질문입니다";
        LocalDate monday = LocalDate.of(2026, 4, 9);
        LocalDate sunday = monday.plusDays(6);
        Question savedQuestion = questionRepository.save(
                new Question(content, QuestionType.CUSTOM, monday, sunday, 1L)
        );

        fakeTimeProvider.changeTime(LocalDateTime.of(2026, 3, 11, 10, 0));

        // when & then
        assertThatCode(() -> questionService.findQuestion(1L, QuestionType.CUSTOM, monday, monday.plusDays(5)))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.QUESTION_NOT_FOUND.getMessage());
    }

    @Test
    void 커스텀_질문이_없다면_예외가_발생한다_그룹ID() {
        // given
        String content = "커스텀 질문입니다";
        LocalDate monday = LocalDate.of(2026, 3, 9);
        LocalDate sunday = monday.plusDays(6);
        Question savedQuestion = questionRepository.save(
                new Question(content, QuestionType.CUSTOM, monday, sunday, 1L)
        );

        fakeTimeProvider.changeTime(LocalDateTime.of(2026, 3, 11, 10, 0));

        // when & then
        assertThatCode(() -> questionService.findQuestion(2L, QuestionType.CUSTOM, monday, sunday))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.QUESTION_NOT_FOUND.getMessage());
    }
}