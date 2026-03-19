package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.comment.domain.Comment;

@Schema(description = "댓글 정보")
public record AdminCommentResponse(
    @Schema(description = "댓글 ID", example = "1")
    Long commentId,

    @Schema(description = "댓글 내용", example = "좋은 하루 보내세요!")
    String content,

    @Schema(description = "작성자 정보")
    AdminCommentAuthorInfo author,

    @Schema(description = "생성 일시", example = "2024-01-20T15:00:00")
    LocalDateTime createdAt,

    @Schema(description = "삭제 일시 (삭제된 경우)", example = "2024-01-25T10:00:00")
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
