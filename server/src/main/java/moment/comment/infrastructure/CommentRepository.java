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
                c.id AS c_id,
                c.commenter_id AS c_commenter_id,
                c.content AS c_content,
                c.created_at AS c_created_at,
                c.deleted_at AS c_deleted_at,
                c.moment_id AS c_moment_id,
                m.id AS m_id,
                m.content AS m_content,
                m.created_at AS m_created_at,
                m.deleted_at AS m_deleted_at,
                m.is_matched AS m_is_matched,
                m.momenter_id AS m_momenter_id,
                m.write_type AS m_write_type,
                u.id AS u_id,
                u.available_star AS u_available_star,
                u.created_at AS u_created_at,
                u.deleted_at AS u_deleted_at,
                u.email AS u_email,
                u.exp_star AS u_exp_star,
                u.level AS u_level,
                u.nickname AS u_nickname,
                u.password AS u_password,
                u.provider_type AS u_provider_type
            FROM
                (SELECT *
                 FROM comments
                 WHERE commenter_id = :#{#commenter.id} AND deleted_at IS NULL
                 ORDER BY created_at DESC, id DESC
                 LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}) AS c
            JOIN
                moments m ON m.id = c.moment_id AND m.deleted_at IS NULL
            JOIN
                users u ON u.id = m.momenter_id AND u.deleted_at IS NULL
            ORDER BY
                c.created_at DESC, c.id DESC;
            """, nativeQuery = true)
    List<Comment> findCommentsFirstPage(@Param("commenter") User commenter, @Param("pageable") Pageable pageable);

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
