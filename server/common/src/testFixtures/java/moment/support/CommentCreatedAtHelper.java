package moment.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CommentCreatedAtHelper {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    CommentRepository commentRepository;

    @Transactional
    public Comment saveCommentWithCreatedAt(String content, User commenter, Long momentId , LocalDateTime createdAt) {
        Comment comment = commentRepository.save(new Comment(content, commenter, momentId));
        entityManager.flush();

        entityManager.createNativeQuery("UPDATE comments SET created_at = ? WHERE id = ?")
                .setParameter(1 , createdAt)
                .setParameter(2 , comment.getId())
                .executeUpdate();

        entityManager.clear();
        return commentRepository.findById(comment.getId()).orElseThrow();
    }
}
