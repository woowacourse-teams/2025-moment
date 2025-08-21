package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record PasswordUpdateRequest(
        @Schema(description = "사용자 이메일", example = "drago93@gamil.com")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
        @Size(max = 255, message = "이메일은 최대 {max}자를 초과할 수 없습니다.")
        String email
) {
}
