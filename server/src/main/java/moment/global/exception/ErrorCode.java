package moment.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_REGISTERED("U-001", "이미 가입된 사용자입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("U-002", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),

    INTERNAL_SERVER_ERROR("G-001", "오류가 발생했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    private final String code;
    private final String message;
    private final HttpStatus status;
}
