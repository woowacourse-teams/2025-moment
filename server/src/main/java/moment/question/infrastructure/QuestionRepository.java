package moment.question.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import moment.global.domain.QuestionType;
import moment.question.domain.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByStartDateAndEndDateAndQuestionType(LocalDate startDate, LocalDate endDate,
                                                                QuestionType type);

    Optional<Question> findByStartDateAndEndDateAndQuestionTypeAndGroupId(LocalDate startDate, LocalDate endDate,
                                                                          QuestionType type, Long groupId);

    List<Question> findByQuestionTypeOrderByStartDateDesc(QuestionType type, Pageable pageable);

    boolean existsByStartDateAndEndDateAndQuestionTypeAndGroupIdIsNull(LocalDate startDate, LocalDate endDate,
                                                                       QuestionType type);
}
