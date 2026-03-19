package moment.question.service.question;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.domain.QuestionType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.Question;
import moment.question.infrastructure.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public List<Question> findRecentQuestions(QuestionType type, int count) {
        Pageable pageable = PageRequest.of(0, count);
        return questionRepository.findByQuestionTypeOrderByStartDateDesc(type, pageable);
    }

    @Transactional
    public void save(String content, QuestionType type, LocalDate startDate, LocalDate endDate, Long groupId) {

        boolean isExist = questionRepository.existsByStartDateAndEndDateAndQuestionTypeAndGroupIdIsNull(
                startDate,
                endDate,
                type);

        if (isExist) {
            throw new MomentException(ErrorCode.QUESTION_ALREADY_EXIST);
        }

        Question question = new Question(content, type, startDate, endDate, groupId);
        questionRepository.save(question);
    }
}
