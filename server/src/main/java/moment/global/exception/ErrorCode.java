package moment.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("G-001", "오류가 발생했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    REQUEST_INVALID("G-002", "유효하지 않은 요청 값입니다.", HttpStatus.BAD_REQUEST),

    USER_CONFLICT("U-001", "이미 가입된 사용자입니다.", HttpStatus.CONFLICT),
    USER_LOGIN_FAILED("U-002", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_NICKNAME_CONFLICT("U-003", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    EMAIL_INVALID("U-004", "유효하지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("U-005", "유효하지 않은 비밀번호 형식입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_INVALID("U-006", "유효하지 않은 닉네임 형식입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCHED("U-007", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_UNAUTHORIZED("U-008", "권한 없는 사용자입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("U-009", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    USER_NICKNAME_GENERATION_FAILED("U-010", "사용 가능한 닉네임을 생성할 수 없습니다.", HttpStatus.CONFLICT),

    TOKEN_INVALID("T-001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("T-002", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EMPTY("T-003", "빈 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_SIGNED("T-004", "서명되지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("T-005", "토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),

    ECHO_NOT_FOUND("E-001", "존재하지 않는 에코입니다.", HttpStatus.BAD_REQUEST),
    ECHO_CONFLICT("E-002", "해당 에코가 이미 존재합니다.", HttpStatus.CONFLICT),

    COMMENT_INVALID("C-001", "유효하지 않은 코멘트입니다.", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND("C-002", "존재하지 않는 코멘트입니다.", HttpStatus.NOT_FOUND),
    COMMENT_CONFLICT("C-003", "모멘트에 등록된 코멘트가 이미 존재합니다.", HttpStatus.CONFLICT),
    COMMENT_CONTENT_INVALID("C-004", "유효하지 않은 코멘트 형식입니다.", HttpStatus.BAD_REQUEST),
    COMMENT_ID_INVALID("C-005", "유효하지 않은 코멘트 ID입니다.", HttpStatus.BAD_REQUEST),
    COMMENTS_LIMIT_INVALID("C-006", "유효하지 않은 페이지 사이즈입니다.", HttpStatus.BAD_REQUEST),
    COMMENTS_LENGTH_INVALID("C-007", "코멘트는 1자 이상, 200자 이하로만 작성 가능합니다.", HttpStatus.BAD_REQUEST),

    MOMENT_CONTENT_EMPTY("M-001", "모멘트 내용이 비어있습니다.", HttpStatus.BAD_REQUEST),
    MOMENT_NOT_FOUND("M-002", "존재하지 않는 모멘트입니다.", HttpStatus.NOT_FOUND),
    MOMENT_ALREADY_EXIST("M-003", "오늘 작성한 모멘트가 이미 존재합니다.", HttpStatus.BAD_REQUEST),
    MOMENT_LENGTH_INVALID("M-004", "모멘트는 1자 이상, 100자 이하로만 작성 가능합니다.", HttpStatus.BAD_REQUEST),
    MOMENTS_LIMIT_INVALID("M-005", "유효하지 않은 페이지 사이즈입니다.", HttpStatus.BAD_REQUEST),

    NOTIFICATION_NOT_FOUND("N-001", "존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND),

    EMAIL_VERIFY_FAILED("V-001", "이메일 인증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_COOL_DOWN_NOT_PASSED("V-002", "이메일 요청은 1분에 한번만 요청 할 수 있습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILURE("V-003", "이메일 전송에 실패했습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
