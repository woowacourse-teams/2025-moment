package moment.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 로그인 요청")
public record AdminLoginRequest(
        @Schema(description = "관리자 이메일", example = "admin@moment.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @Schema(description = "비밀번호", example = "Password123!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        String password
) {
}
