package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest (

    @Schema(description = "사용자 이메일", example = "user@example.com")
    String email,

    @Schema(description = "이메일로 발송된 비밀번호 재설정 토큰")
    String token,

    @Schema(description = "새로운 비밀번호", example = "newPassword123!")
    String newPassword,

    @Schema(description = "새로운 비밀번호 확인", example = "newPassword123!")
    String newPasswordCheck
    ) {
}
