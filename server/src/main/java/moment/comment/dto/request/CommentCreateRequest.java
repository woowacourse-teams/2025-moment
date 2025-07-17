package moment.comment.dto.request;

import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public record CommentCreateRequest(String content, Long momentId) {
    public Comment toComment(User commenter, Moment moment) {
        return new Comment(content, commenter, moment);
    }
}
