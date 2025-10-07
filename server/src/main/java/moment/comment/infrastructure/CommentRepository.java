package moment.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.springframework.data.domain.Pageable;
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
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findCommentsByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT c
            FROM comments c
            WHERE c.id IN :ids
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsFirstPage(@Param("ids") List<Long> ids, Pageable pageable);

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

    @Query("""
            SELECT c FROM comments c
            WHERE c.id IN :ids AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findUnreadCommentsNextPage(@Param("ids") List<Long> ids,
                                             @Param("cursorTime") LocalDateTime cursorDateTime,
                                             @Param("cursorId") Long cursorId,
                                             Pageable pageable);

    List<Comment> findAllByMomentIdIn(List<Long> momentIds);

    @Query("""
            SELECT c.momentId
            FROM comments c
            WHERE c.momentId IN :momentIds
            AND c.commenter.id <> :commenterId
            """)
    List<Long> findMomentIdsCommentedOnByOthers(@Param("momentIds") List<Long> momentIds,
                                                @Param("commenterId") Long commenterId);

    boolean existsByMomentIdAndCommenterId(Long momentId, Long commenterId);

    Optional<Long> findMomentIdById(Long commentId);
}
