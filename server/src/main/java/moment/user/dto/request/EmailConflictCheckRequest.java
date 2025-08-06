package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 중복 확인 요청")
public record EmailConflictCheckRequest(
        @Schema(description = "중복 확인 이메일", example = "mimi@icloud.com")
        @Pattern(regexp = "^.{1,15}$", message = "EMAIL_INVALID")
        @NotBlank(message = "EMAIL_INVALID")
        String email
) {
}
