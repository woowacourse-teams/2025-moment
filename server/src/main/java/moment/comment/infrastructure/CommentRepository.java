package moment.comment.infrastructure;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"moment"})
    List<Comment> findCommentsByCommenterIdOrderByCreatedAtDesc(Long commenterId);

    @EntityGraph(attributePaths = {"moment"})
    List<Comment> findAllByMomentIn(List<Moment> moments);

    boolean existsByMoment(Moment moment);
}
