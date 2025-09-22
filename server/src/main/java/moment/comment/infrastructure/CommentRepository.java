package moment.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = """
            SELECT
                c1_0.id, c1_0.commenter_id, c1_0.content, c1_0.created_at, c1_0.deleted_at, c1_0.moment_id,
                m1_0.id, m1_0.content, m1_0.created_at, m1_0.deleted_at, m1_0.is_matched, m1_0.momenter_id,
                m2_0.id, m2_0.available_star, m2_0.created_at, m2_0.deleted_at, m2_0.email, m2_0.exp_star, m2_0.level, m2_0.nickname, m2_0.password, m2_0.provider_type, m1_0.write_type
            FROM
                (SELECT *
                 FROM comments
                 WHERE commenter_id = :#{#commenter.id} AND deleted_at IS NULL
                 ORDER BY created_at DESC, id DESC
                 LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}) AS c1_0) AS c1_0
            JOIN
                moments m1_0 ON m1_0.id = c1_0.moment_id AND m1_0.deleted_at IS NULL
            JOIN
                users m2_0 ON m2_0.id = m1_0.momenter_id AND m2_0.deleted_at IS NULL
            ORDER BY
                c1_0.created_at DESC, c1_0.id DESC;
            """, nativeQuery = true)
    List<Comment> findCommentsFirstPage(@Param("commenter") User commenter, Pageable pageable);

    @EntityGraph(attributePaths = {"moment.momenter"})
    @Query("""
            SELECT c FROM comments c
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsFirstPage(@Param("ids") Set<Long> ids, Pageable pageable);

    @EntityGraph(attributePaths = {"moment.momenter"})
    @Query("""
            SELECT c FROM comments c
            WHERE c.commenter = :commenter AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findCommentsNextPage(@Param("commenter") User commenter,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @EntityGraph(attributePaths = {"moment.momenter"})
    @Query("""
            SELECT c FROM comments c
            WHERE c.id IN :ids AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsNextPage(@Param("ids") Set<Long> ids,
                                       @Param("cursorTime") LocalDateTime cursorDateTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @EntityGraph(attributePaths = {"moment"})
    List<Comment> findAllByMomentIn(List<Moment> moments);

    boolean existsByMomentAndCommenter(Moment moment, User commenter);

    long countByMomentAndCreatedAtBetween(Moment moment, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"commenter"})
    Optional<Comment> findWithCommenterById(Long id);
}
