package moment.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        String password
) {
}
