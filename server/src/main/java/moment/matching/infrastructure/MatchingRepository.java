package moment.matching.infrastructure;

import java.time.LocalDate;
import java.util.List;
import moment.matching.domain.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    @Query("SELECT m FROM matchings m WHERE FUNCTION('DATE', m.createdAt) = :today")
    List<Matching> findAllByCreatedDate(@Param("today") LocalDate today);
}
