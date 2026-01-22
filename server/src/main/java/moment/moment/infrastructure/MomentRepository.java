package moment.moment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.momenter = :momenter
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentFirstPage(@Param("momenter") User momenter, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.id IN :ids
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentFirstPage(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.momenter = :momenter AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentsNextPage(@Param("momenter") User momenter,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            JOIN FETCH m.momenter
            WHERE m.id IN :ids AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentNextPage(@Param("ids") List<Long> ids,
                                            @Param("cursorTime") LocalDateTime cursorDateTime,
                                            @Param("cursorId") Long cursorId,
                                            Pageable pageable);

    @Query("""
        SELECT m.id FROM moments m
        WHERE 
            m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
            AND m.id NOT IN :reportedMoments 
    """)
    List<Long> findMomentIdsExcludingReported(@Param("userId") Long userId,
                                              @Param("someDaysAgo") LocalDateTime someDaysAgo,
                                              @Param("reportedMoments") List<Long> reportedMoments);

    @Query("""
        SELECT m.id FROM moments m
        WHERE 
            m.momenter.id <> :userId
            AND m.createdAt >= :someDaysAgo
    """)
    List<Long> findMomentIds(
            @Param("userId") Long userId,
            @Param("someDaysAgo") LocalDateTime someDaysAgo);

    void deleteById(Long momentId);

    @Query("""
          SELECT m
          FROM moments m
          JOIN FETCH m.momenter
          WHERE m.id IN :momentIds
           """)
    List<Moment> findAllWithMomenterByIds(@Param("momentIds")List<Long> momentIds);
}
