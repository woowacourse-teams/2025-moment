package moment.question.service.question;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import moment.global.domain.QuestionType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.Question;
import moment.question.infrastructure.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Question findQuestion(Long groupId, QuestionType type, LocalDate startDate, LocalDate endDate) {
        if (type == QuestionType.COMMON) {
            return findCommonQuestion(startDate, endDate, type);
        }

        return findCustomQuestion(startDate, endDate, type, groupId);
    }

    private Question findCommonQuestion(LocalDate startDate, LocalDate endDate, QuestionType type) {
        return questionRepository
                .findByStartDateAndEndDateAndQuestionType(startDate, endDate, type)
                .orElseThrow(() -> new MomentException(ErrorCode.QUESTION_NOT_FOUND));
    }

    private Question findCustomQuestion(LocalDate startDate, LocalDate endDate, QuestionType type, Long groupId) {
        return questionRepository
                .findByStartDateAndEndDateAndQuestionTypeAndGroupId(startDate, endDate, type, groupId)
                .orElseThrow(() -> new MomentException(ErrorCode.QUESTION_NOT_FOUND));
    }
}
