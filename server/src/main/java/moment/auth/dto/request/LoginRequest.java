package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Size(max = 255, message = "이메일은 최대 {max}자를 초과할 수 없습니다.")
        String email,

        @Schema(description = "사용자 비밀번호", example = "1234")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
        String password
) {
}
