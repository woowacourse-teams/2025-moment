package moment.moment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    List<Moment> findMomentByMomenterOrderByCreatedAtDesc(User Momenter);

    @Query("""
            SELECT ma.moment FROM matchings ma
                WHERE ma.commenter = :commenter AND ma.createdAt BETWEEN :startOfDay AND :endOfDay
            """
    )
    Optional<Moment> findMatchedMomentByCommenter(@Param("commenter") User commenter,
                                                  @Param("startOfDay") LocalDateTime startOfDay,
                                                  @Param("endOfDay") LocalDateTime endOfDay);

    int countByMomenterAndCreatedAtBetween(User user, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
