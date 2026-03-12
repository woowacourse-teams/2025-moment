package moment.question.service.facade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import moment.global.domain.QuestionType;
import moment.global.domain.TimeProvider;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.service.group.GroupService;
import moment.question.domain.Question;
import moment.question.domain.QuestionCycle;
import moment.question.dto.response.QuestionResponse;
import moment.question.service.question.QuestionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionFacadeService {

    private final QuestionService questionService;
    private final GroupService groupService;
    private final TimeProvider timeProvider;

    public QuestionResponse findCurrentQuestion(Long groupId, Long userId, QuestionCycle cycle) {
        // 일단은 공통 질문으로 고정, 나중에는 그룹 서비스에서 너 현재 질문 상태 뭐야? 라고 물어봐야함.
        QuestionType currentType = QuestionType.COMMON;
        LocalDate today = timeProvider.now().toLocalDate();

        DateRange dateRange = calculateDateRange(cycle, today);

        Question currentQuestion = questionService.findQuestion(
                groupId,
                currentType,
                dateRange.startDate(),
                dateRange.endDate()
        );

        return QuestionResponse.from(currentQuestion);
    }
    
    private DateRange calculateDateRange(QuestionCycle cycle, LocalDate today) {
        if (cycle == QuestionCycle.DAILY) {
            return new DateRange(today, today);
        }

        if (cycle == QuestionCycle.WEEKLY) {
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return new DateRange(monday, monday.plusDays(6));
        }

        if (cycle == QuestionCycle.MONTHLY) {
            LocalDate firstDay = today.withDayOfMonth(1);
            LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange(firstDay, lastDay);
        }

        throw new MomentException(ErrorCode.QUESTION_CYCLE_NOT_SUPPORT);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
