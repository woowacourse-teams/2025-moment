package moment.comment.infrastructure;

import java.util.List;
import moment.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT DISTINCT c FROM comments c " +
            "JOIN FETCH c.moment m " +
            "LEFT JOIN FETCH c.emojis e " +
            "WHERE c.commenter.id = :commenterId")
    List<Comment> findCommentsWithMomentAndEmojisByCommenterId(Long commenterId);
}
