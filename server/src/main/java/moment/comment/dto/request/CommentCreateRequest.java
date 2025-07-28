package moment.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.user.domain.User;

@Schema(description = "Moment 등록 요청 DTO")
public record CommentCreateRequest(
        @Schema(description = "Comment 내용", example = "정말 멋진 하루군요!")
        @NotBlank
        String content,

        @Schema(description = "Moment 아이디", example = "1")
        @NotNull
        Long momentId
) {
    public Comment toComment(User commenter, Moment moment) {
        return new Comment(content, commenter, moment);
    }
}
