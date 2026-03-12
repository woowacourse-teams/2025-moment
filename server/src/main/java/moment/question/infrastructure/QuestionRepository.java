package moment.question.infrastructure;

import java.time.LocalDate;
import java.util.Optional;
import moment.global.domain.QuestionType;
import moment.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByStartDateAndEndDateAndQuestionType(LocalDate startDate, LocalDate endDate,
                                                                QuestionType type);

    Optional<Question> findByStartDateAndEndDateAndQuestionTypeAndGroupId(LocalDate startDate, LocalDate endDate,
                                                                          QuestionType type, Long groupId);
}
