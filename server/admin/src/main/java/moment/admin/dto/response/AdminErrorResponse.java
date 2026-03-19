package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.admin.global.exception.AdminErrorCode;

@Schema(description = "Admin API 에러 응답")
public record AdminErrorResponse(
    @Schema(description = "에러 코드", example = "AG-001")
    String code,

    @Schema(description = "에러 메시지", example = "그룹을 찾을 수 없습니다.")
    String message,

    @Schema(description = "HTTP 상태 코드", example = "404")
    int status
) {
    public static AdminErrorResponse from(AdminErrorCode errorCode) {
        return new AdminErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            errorCode.getStatus().value()
        );
    }
}
