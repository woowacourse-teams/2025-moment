package moment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank(message = "identityToken은 필수입니다.")
        String identityToken
) {
}
