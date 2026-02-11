package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.domain.CommentCreationStatus;

@Schema(description = "Comment 등록 전 상태 체크 응답")
public record CommentCreationStatusResponse(
        @Schema(description = "코멘트 등록 가능 상태", example = "WRITABLE")
        CommentCreationStatus commentCreationStatus
) {
    public static CommentCreationStatusResponse from(CommentCreationStatus commentCreationStatus) {
        return new CommentCreationStatusResponse(commentCreationStatus);
    }
}
