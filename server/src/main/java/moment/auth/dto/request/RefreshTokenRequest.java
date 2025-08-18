package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "엑세스 토큰 재발급 요청")
public record RefreshTokenRequest(
        @NotBlank(message = "REFRESH_TOKEN_EMPTY")
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NTU0NDE2MDMsInN1YiI6IjEiLCJlbWFpbCI6ImVrb3JlYTYyM0BnbWFpbC5jb20iLCJpYXQiOjE3NTU0Mzk4MDN9.6Y9r8he8ZOIaXthdxK9ofXtcKbtZH3PXc1WFEjKdCUQf")
        String refreshToken
) {
}
