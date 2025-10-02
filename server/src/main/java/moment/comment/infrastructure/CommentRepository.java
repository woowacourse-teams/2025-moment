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

    @Query("""
                SELECT c.id
                FROM comments c
                WHERE c.commenter = :commenter
                ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Long> findFirstPageCommentIdsByCommenter(@Param("commenter") User commenter, Pageable pageable);

    @Query("""
            SELECT c
            FROM comments c
            LEFT JOIN FETCH c.moment m
            LEFT JOIN FETCH m.momenter
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findCommentsWithDetailsByIds(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"moment.momenter"})
    @Query("""
            SELECT c
            FROM comments c
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsFirstPage(@Param("ids") Set<Long> ids, Pageable pageable);

    @Query("""
                SELECT c.id
                FROM comments c
                WHERE c.commenter = :commenter AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
                ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Long> findNextPageCommentIdsByCommenter(@Param("commenter") User commenter,
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

    List<Comment> findAllByMomentIdIn(List<Long> momentIds);
}
