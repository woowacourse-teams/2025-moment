package moment.question.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import moment.config.TestTags;
import moment.global.domain.QuestionType;
import moment.global.domain.TimeProvider;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.service.group.GroupService;
import moment.question.domain.Question;
import moment.question.domain.QuestionCycle;
import moment.question.dto.response.QuestionResponse;
import moment.question.infrastructure.QuestionRepository;
import moment.question.service.question.QuestionService;
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

    @Autowired
    private TimeProvider timeProvider;
    private QuestionFacadeService questionFacadeService;

    @BeforeEach
    void setUp() {
        LocalDate today = timeProvider.now().toLocalDate();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        LocalDate firstDate = today.withDayOfMonth(1);
        LocalDate lastDate = today.with(TemporalAdjusters.lastDayOfMonth());

        questionFacadeService = new QuestionFacadeService(questionService, groupService, timeProvider);

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
}
