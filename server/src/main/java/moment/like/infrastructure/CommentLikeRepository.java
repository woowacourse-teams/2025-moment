package moment.like.infrastructure;

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
}
