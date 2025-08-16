package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증 코드 확인 요청")
public record EmailVerifyRequest(
    @Schema(description = "사용자 이메일", example = "drago93@gamil.com")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @Schema(description = "인증 코드", example = "123456")
    @NotBlank(message = "인증 코드는 필수입니다.")
    String code
) {
}
