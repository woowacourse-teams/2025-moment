package moment.question.infrastructure;

import java.util.Optional;
import moment.question.domain.FallbackQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FallbackQuestionRepository extends JpaRepository<FallbackQuestion, Long> {

    Optional<FallbackQuestion> findFirstByIsUsedFalse();
}
