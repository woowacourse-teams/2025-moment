package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @Schema(description = "새 비밀번호", example = "hipopo12!")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
        @NotBlank(message = "PASSWORD_INVALID")
        String newPassword,

        @Schema(description = "비밀번호 확인", example = "hipopo12!")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
        @NotBlank(message = "PASSWORD_INVALID")
        String checkedPassword
) {
}
