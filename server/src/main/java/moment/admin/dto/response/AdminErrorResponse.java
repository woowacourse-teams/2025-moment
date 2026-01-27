package moment.admin.dto.response;

import moment.admin.global.exception.AdminErrorCode;

public record AdminErrorResponse(
    String code,
    String message,
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
