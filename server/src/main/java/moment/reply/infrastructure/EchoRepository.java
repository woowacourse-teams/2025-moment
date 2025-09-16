package moment.reply.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import moment.comment.domain.Comment;
import moment.reply.domain.Echo;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EchoRepository extends JpaRepository<Echo, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Echo> findAllByComment(Comment comment);

    @EntityGraph(attributePaths = {"comment"})
    List<Echo> findAllByCommentIn(List<Comment> comments);

    List<Echo> findByCommentAndUserAndEchoTypeIn(Comment comment, User user, Collection<String> echoTypes);

    void deleteByComment(Comment comment);

    Optional<Echo> findByComment(Comment savedComment);
}
