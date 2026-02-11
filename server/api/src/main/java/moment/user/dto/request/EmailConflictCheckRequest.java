package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 중복 확인 요청")
public record EmailConflictCheckRequest(
        @Schema(description = "중복 확인 이메일", example = "mimi@icloud.com")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
        String email
) {
}
