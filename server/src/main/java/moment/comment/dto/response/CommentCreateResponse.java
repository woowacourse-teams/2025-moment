package moment.comment.dto.response;

import moment.comment.domain.Comment;

public record CommentCreateResponse(Long commentId, String content) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(comment.getId(), comment.getContent());
    }
}
