package moment.comment.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.comment.domain.Comment;

@Schema(description = "Comment 등록 응답")
public record CommentCreateResponse(
        @Schema(description = "등록된 Comment 아이디", example = "1")
        Long commentId,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content

        //todo 생성 후 반환값에 created_at 포함하기
) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(comment.getId(), comment.getContent());
    }
}
