package moment.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("G-001", "오류가 발생했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_CONFLICT("U-001", "이미 가입된 사용자입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("U-002", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_NICKNAME_CONFLICT("U-003", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    EMAIL_INVALID("U-004", "유효하지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("U-005", "유효하지 않은 비밀번호 형식입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_INVALID("U-006", "유효하지 않은 닉네임 형식입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCHED("U-007", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    TOKEN_INVALID("T-001", "유효하지 않은 토큰입니다." , HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("T-002", "만료된 토큰입니다." , HttpStatus.UNAUTHORIZED),
    TOKEN_EMPTY("T-003", "빈 토큰입니다." , HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_SIGNED("T-004", "서명되지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("T-005", "토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    private final String code;
    private final String message;
    private final HttpStatus status;
}
