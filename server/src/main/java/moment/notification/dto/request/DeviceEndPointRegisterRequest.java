package moment.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 정보 등록 요청")
public record DeviceEndPointRegisterRequest(
        @Schema(description = "디바이스 정보", example = "a1b2c3d4f5")
        @NotBlank
        String deviceEndpoint) {
}
