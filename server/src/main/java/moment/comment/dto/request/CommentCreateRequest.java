package moment.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import moment.comment.domain.Comment;
import moment.user.domain.User;

@Schema(description = "코멘트 등록 요청")
public record CommentCreateRequest(
        @Schema(description = "Comment 내용", example = "정말 멋진 하루군요!")
        @NotBlank(message = "COMMENT_CONTENT_INVALID")
        @Size(min = 1, max = 200, message = "COMMENTS_LENGTH_INVALID")
        String content,

        @Schema(description = "모멘트 아이디", example = "1")
        @NotNull(message = "COMMENT_ID_INVALID")
        Long momentId,

        @Schema(
                description = "코멘트 사진 저장 경로",
                example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String imageUrl,

        @Schema(
                description = "코멘트 사진 이름",
                example = "고양이.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String imageName
) {
    public Comment toComment(User commenter, Long momentId) {
        return new Comment(content, commenter, momentId);
    }
}
