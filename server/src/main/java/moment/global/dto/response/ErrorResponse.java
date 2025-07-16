package moment.global.dto.response;

import moment.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public record ErrorResponse(String code, String message, HttpStatus status) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errorCode.getStatus());
    }
}
