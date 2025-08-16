package moment.comment.application;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;

public interface CommentQueryService {

    Comment getCommentById(Long id);

    boolean existsByMoment(Moment moment);
}
