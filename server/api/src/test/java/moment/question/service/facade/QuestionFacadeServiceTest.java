package moment.question.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import moment.config.TestTags;
import moment.global.domain.QuestionType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.service.group.GroupService;
import moment.question.domain.FallbackQuestion;
import moment.question.domain.Question;
import moment.question.domain.QuestionCycle;
import moment.question.dto.response.QuestionResponse;
import moment.question.infrastructure.FallbackQuestionRepository;
import moment.question.infrastructure.QuestionRepository;
import moment.question.service.question.FallbackQuestionService;
import moment.question.service.question.QuestionService;
import moment.support.FakeQuestionGenerator;
import moment.support.FakeTimeProvider;
import moment.support.TimeProviderTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
@Import(TimeProviderTestConfig.class) // 해당 시간 설정을 사용합니다.
class QuestionFacadeServiceTest {

    private final String dailyContent = "일간질문";
    private final String weeklyContent = "주간질문";
    private final String monthlyContent = "월간질문";

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private GroupService groupService;

    private FakeTimeProvider timeProvider;

    private FakeQuestionGenerator questionGenerator;

    @Autowired
    private FallbackQuestionService fallbackQuestionService;

    private QuestionFacadeService questionFacadeService;
    @Autowired
    private FallbackQuestionRepository fallbackQuestionRepository;

    @BeforeEach
    void setUp() {
        timeProvider = new FakeTimeProvider(LocalDateTime.now());
        LocalDate today = timeProvider.now().toLocalDate();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        LocalDate firstDate = today.withDayOfMonth(1);
        LocalDate lastDate = today.with(TemporalAdjusters.lastDayOfMonth());
        questionGenerator = new FakeQuestionGenerator();

        questionFacadeService = new QuestionFacadeService(questionService, groupService, timeProvider,
                questionGenerator, fallbackQuestionService);

        questionRepository.saveAll(
                List.of(
                        new Question(dailyContent, QuestionType.COMMON, today, today, null),
                        new Question(weeklyContent, QuestionType.COMMON, monday, sunday, null),
                        new Question(monthlyContent, QuestionType.COMMON, firstDate, lastDate, null)
                )
        );
    }

    @Test
    void 일간_질문을_찾는다() {
        // given
        QuestionCycle cycle = QuestionCycle.DAILY;

        // when
        QuestionResponse response = questionFacadeService.findCurrentQuestion(null, null, cycle);

        // then
        assertThat(response.content()).isEqualTo(dailyContent);
    }

    @Test
    void 주간_질문을_찾는다() {
        // given
        QuestionCycle cycle = QuestionCycle.WEEKLY;

        // when
        QuestionResponse response = questionFacadeService.findCurrentQuestion(null, null, cycle);

        // then
        assertThat(response.content()).isEqualTo(weeklyContent);
    }

    @Test
    void 월간_질문을_찾는다() {
        // given
        QuestionCycle cycle = QuestionCycle.MONTHLY;

        // when
        QuestionResponse response = questionFacadeService.findCurrentQuestion(null, null, cycle);

        // then
        assertThat(response.content()).isEqualTo(monthlyContent);
    }

    @Test
    void 잘못된_사이클을_받으면_예외가_발생한다() {
        // given
        QuestionCycle cycle = null;

        // when & then
        assertThatCode(() -> questionFacadeService.findCurrentQuestion(null, null, cycle))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.QUESTION_CYCLE_NOT_SUPPORT.getMessage());
    }

    @Test
    void AI_질문_생성_성공시_임시질문을_조회하지_않고_즉시_저장한다() {
        // given
        String nextQuestion = "AI가 만든 멋진 질문";
        LocalDate today = LocalDate.of(2025, 1, 1);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        timeProvider.changeTime(today.atTime(10, 0));
        questionGenerator.setNextQuestion(nextQuestion);

        // when
        questionFacadeService.generateWeeklyCommonQuestion();
        Optional<Question> question = questionRepository.findByStartDateAndEndDateAndQuestionType(monday, sunday,
                QuestionType.COMMON);

        // then
        assertAll(
                () -> assertThat(question).isPresent(),
                () -> assertThat(question.get().getContent()).isEqualTo(nextQuestion)
        );
    }

    @Test
    void AI_질문_생성_실패시_임시질문을_가져와_저장하고_사용처리한다() {
        // given
        questionGenerator.setShouldThrowException(true);
        String content = "DB에 있던 예비 질문";
        FallbackQuestion fallbackQuestion = fallbackQuestionRepository.save(new FallbackQuestion(content));

        LocalDate today = LocalDate.of(2025, 1, 1);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        timeProvider.changeTime(today.atTime(10, 0));

        // when
        questionFacadeService.generateWeeklyCommonQuestion();
        Optional<Question> question = questionRepository.findByStartDateAndEndDateAndQuestionType(monday, sunday,
                QuestionType.COMMON);

        // then
        assertAll(
                () -> assertThat(question).isPresent(),
                () -> assertThat(question.get().getContent()).isEqualTo(content),
                () -> assertThat(fallbackQuestion.isUsed()).isTrue()
        );
    }

    @Test
    void AI_생성_실패_후_남은_임시질문도_없으면_예외를_상위로_전파한다() {
        // given
        questionGenerator.setShouldThrowException(true);
        fallbackQuestionRepository.deleteAll();

        // when & then
        assertThatThrownBy(() -> questionFacadeService.generateWeeklyCommonQuestion())
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.QUESTION_FALLBACK_EXHAUSTED.getMessage());

    }
}
