package moment.global.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.global.exception.ErrorCode;

@Schema(description = "예외 공통 응답 DTO")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "U-001")
        String code,

        @Schema(description = "예외 메시지", example = "이미 가입된 사용자입니다.")
        String message,

        @Schema(description = "상태 코드", example = "409")
        int status
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errorCode.getStatus().value());
    }
}
