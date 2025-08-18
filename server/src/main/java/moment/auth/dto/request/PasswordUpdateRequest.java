package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordUpdateRequest(

        @Schema(description = "사용자 이메일", example = "drago93@gamil.com")
        String email
) {
}
