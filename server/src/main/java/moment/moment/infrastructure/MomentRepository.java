package moment.moment.infrastructure;

import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    @Query("""
            SELECT m FROM moments m
            WHERE m.momenter = :momenter
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentFirstPage(@Param("momenter") User momenter, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            WHERE m.momenter = :momenter AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentsNextPage(@Param("momenter") User momenter,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    int countByMomenterAndWriteTypeAndCreatedAtBetween(
            User user,
            WriteType writeType,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
