package moment.comment.infrastructure;

import java.util.List;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"moment"})
    List<Comment> findAllByMomentIn(List<Moment> moments);
}
