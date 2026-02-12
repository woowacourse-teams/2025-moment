package moment.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
        @Schema(description = "디바이스 푸시 토큰 (선택)",
                example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]", nullable = true)
        @Size(max = 255, message = "디바이스 토큰은 최대 255자를 초과할 수 없습니다.")
        String deviceEndpoint
) {}
