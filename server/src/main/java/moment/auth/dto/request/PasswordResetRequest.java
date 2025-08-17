package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest (

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @Schema(description = "이메일로 발송된 비밀번호 재설정 토큰")
    @NotBlank(message = "토큰은 필수입니다.")
    String token,

    @Schema(description = "새로운 비밀번호", example = "newPassword123!")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
    String newPassword,

    @Schema(description = "새로운 비밀번호 확인", example = "newPassword123!")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
    String newPasswordCheck
    ) {
}
