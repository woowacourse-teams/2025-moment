package moment.comment.infrastructure;

import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentImageRepository extends JpaRepository<CommentImage, Long> {
    
    @EntityGraph(attributePaths = {"comment"})
    List<CommentImage> findAllByCommentIn(List<Comment> comments);
}
