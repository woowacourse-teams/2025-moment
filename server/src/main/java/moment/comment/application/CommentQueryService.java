package moment.comment.application;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public interface CommentQueryService {

    Comment getCommentById(Long id);

    boolean existsByMomentAndUser(Moment moment, User user);
}
