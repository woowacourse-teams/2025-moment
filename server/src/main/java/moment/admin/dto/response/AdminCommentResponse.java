package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.comment.domain.Comment;

public record AdminCommentResponse(
    Long commentId,
    String content,
    AdminCommentAuthorInfo author,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminCommentResponse from(Comment comment) {
        return new AdminCommentResponse(
            comment.getId(),
            comment.getContent(),
            AdminCommentAuthorInfo.from(comment.getMember()),
            comment.getCreatedAt(),
            comment.getDeletedAt()
        );
    }
}
