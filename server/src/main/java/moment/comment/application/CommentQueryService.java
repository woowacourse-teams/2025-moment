package moment.comment.application;

import moment.comment.domain.Comment;

public interface CommentQueryService {

    Comment getCommentById(Long id);
}
