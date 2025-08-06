package moment.reply.infrastructure;

import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Emoji> findAllByComment(Comment comment);

    @EntityGraph(attributePaths = {"comment"})
    List<Emoji> findAllByCommentIn(List<Comment> comments);

    boolean existsByComment(Comment comment);
}
