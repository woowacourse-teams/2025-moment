package moment.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;

@Schema(description = "코멘트 등록 요청")
public record CommentCreateRequest(
        @Schema(description = "Comment 내용", example = "정말 멋진 하루군요!")
        @NotBlank(message = "COMMENT_CONTENT_INVALID")
        @Size(min = 1, max = 200, message = "COMMENTS_LENGTH_INVALID")
        String content,

        @Schema(description = "모멘트 아이디", example = "1")
        @NotNull(message = "COMMENT_ID_INVALID")
        Long momentId
) {
    public Comment toComment(User commenter, Moment moment) {
        return new Comment(content, commenter, moment);
    }
}
