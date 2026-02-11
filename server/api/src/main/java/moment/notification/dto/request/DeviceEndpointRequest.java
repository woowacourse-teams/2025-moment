package moment.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 Expo Push Token 요청")
public record DeviceEndpointRequest(
        @Schema(description = "Expo Push Token", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
        @NotBlank
        String deviceEndpoint) {
}
