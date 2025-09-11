package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.comment.domain.Comment;

@Schema(description = "Comment 등록 응답")
public record CommentCreateResponse(
        @Schema(description = "등록된 Comment 아이디", example = "1")
        Long commentId,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(comment.getId(), comment.getContent(), comment.getCreatedAt());
    }
}
