package moment.moment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            WHERE m.id IN :ids
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentFirstPage(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            WHERE m.momenter = :momenter AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyMomentsNextPage(@Param("momenter") User momenter,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @Query("""
            SELECT m FROM moments m
            WHERE m.id IN :ids AND (m.createdAt < :cursorTime OR (m.createdAt = :cursorTime AND m.id < :cursorId))
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Moment> findMyUnreadMomentNextPage(@Param("ids") List<Long> ids,
                                            @Param("cursorTime") LocalDateTime cursorDateTime,
                                            @Param("cursorId") Long cursorId,
                                            Pageable pageable);

    int countByMomenterAndWriteTypeAndCreatedAtBetween(
            User user,
            WriteType writeType,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
            SELECT m FROM moments m
            WHERE m.momenter <> :user
              AND m.createdAt >= :someDaysAgo
              AND m.id NOT IN :reportedMoments
            """)
    List<Moment> findAllExceptUser(@Param("user") User user,
                                   @Param("someDaysAgo") LocalDateTime someDaysAgo,
                                   @Param("reportedMoments") List<Long> reportedMoments);

//    @Query("""
//            SELECT m FROM moments m
//            LEFT JOIN moment_tags mt ON mt.moment = m
//            LEFT JOIN tags t ON t = mt.tag
//            WHERE m.momenter <> :user
//              AND NOT EXISTS (
//                  SELECT 1 FROM comments c
//                  WHERE c.moment = m
//                    AND c.commenter = :user
//              )
//              AND m.createdAt >= :someDaysAgo
//              AND (t.name IN :tagNames)
//              AND m.id NOT IN :reportedMoments
//            """)
//    List<Moment> findCommentableMomentsByTagNames(@Param("user") User user,
//                                                  @Param("someDaysAgo") LocalDateTime someDaysAgo,
//                                                  @Param("tagNames") List<String> tagNames,
//                                                  @Param("reportedMoments") List<Long> reportedMoments);

    List<Moment> findByMomenterAndCreatedAtAfter(User momenter, LocalDateTime dateTime);

    @EntityGraph(attributePaths = {"momenter"})
    Optional<Moment> findWithMomenterByid(Long id);

    void deleteById(Long momentId);
}
