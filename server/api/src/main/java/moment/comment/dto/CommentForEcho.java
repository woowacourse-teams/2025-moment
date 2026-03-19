package moment.comment.dto;

import moment.comment.domain.Comment;

public record CommentForEcho(Long momentId, Long commenterId) {

    public static CommentForEcho from(Comment comment) {
        return new CommentForEcho(comment.getMomentId(), comment.getCommenter().getId());
    }
}
