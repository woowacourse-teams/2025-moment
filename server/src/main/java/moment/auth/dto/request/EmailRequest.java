package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 요청")
public record EmailRequest(

        @Schema(description = "사용자 이메일", example = "drago93@gamil.com")
        String email
) {
}
