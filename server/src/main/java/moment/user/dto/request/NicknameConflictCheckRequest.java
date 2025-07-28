package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "닉네임 중복 확인 요청")
public record NicknameConflictCheckRequest(
        @Schema(description = "중복 확인 닉네임", example = "mimi")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,6}$", message = "NICKNAME_INVALID")
        @NotBlank(message = "NICKNAME_INVALID")
        String nickname
) {
}
