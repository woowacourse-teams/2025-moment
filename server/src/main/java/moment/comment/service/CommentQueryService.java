package moment.comment.service;

import java.util.List;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public interface CommentQueryService {

    Comment getCommentById(Long id);

    boolean existsByMomentAndCommenter(Moment moment, User commenter);

    List<Comment> getAllByMomentIn(List<Moment> moments);

    Comment getCommentWithCommenterById(Long id);
}
