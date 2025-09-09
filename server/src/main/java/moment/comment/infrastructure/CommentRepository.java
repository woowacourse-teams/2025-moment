package moment.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"moment.momenter"})
    @Query("""
            SELECT c FROM comments c
            WHERE c.commenter = :commenter
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findCommentsFirstPage(@Param("commenter") User commenter, Pageable pageable);

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

    @EntityGraph(attributePaths = {"moment"})
    List<Comment> findAllByMomentIn(List<Moment> moments);

    boolean existsByMomentAndCommenter(Moment moment, User commenter);

    long countByMomentAndCreatedAtBetween(Moment moment, LocalDateTime start, LocalDateTime end);
}
