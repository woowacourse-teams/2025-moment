package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 요청")
public record EmailRequest(
        @Schema(description = "사용자 이메일", example = "drago93@gamil.com")
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Size(max = 255, message = "이메일은 최대 {max}자를 초과할 수 없습니다.")
        String email
) {
}
