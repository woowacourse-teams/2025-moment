package moment.like.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.like.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId);

    @Query(value = "SELECT * FROM comment_likes WHERE comment_id = :commentId AND member_id = :memberId",
           nativeQuery = true)
    Optional<CommentLike> findByCommentIdAndMemberIdIncludeDeleted(
        @Param("commentId") Long commentId,
        @Param("memberId") Long memberId
    );

    long countByCommentId(Long commentId);

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    @Query("""
            SELECT cl.comment.id, COUNT(cl)
            FROM CommentLike cl
            WHERE cl.comment.id IN :commentIds
            GROUP BY cl.comment.id
            """)
    List<Object[]> countByCommentIdIn(@Param("commentIds") List<Long> commentIds);

    @Query("""
            SELECT cl.comment.id
            FROM CommentLike cl
            WHERE cl.comment.id IN :commentIds AND cl.member.id = :memberId
            """)
    List<Long> findLikedCommentIds(
            @Param("commentIds") List<Long> commentIds,
            @Param("memberId") Long memberId
    );
}
