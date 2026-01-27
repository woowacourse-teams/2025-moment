package moment.admin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode {
    // 인증/인가
    LOGIN_FAILED("A-001", "관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND("A-002", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("A-003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 비즈니스 규칙
    DUPLICATE_EMAIL("A-004", "이미 등록된 관리자 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_INPUT("A-005", "유효하지 않은 입력 정보입니다.", HttpStatus.BAD_REQUEST),
    CANNOT_BLOCK_SELF("A-006", "자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_BLOCK_LAST_SUPER_ADMIN("A-007", "마지막 SUPER_ADMIN은 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 세션
    SESSION_NOT_FOUND("A-008", "세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_EXPIRED("A-009", "세션이 만료되었습니다. 다시 로그인해 주세요.", HttpStatus.UNAUTHORIZED),

    // User 관련 (Admin이 User를 관리할 때 사용)
    USER_NOT_FOUND("A-010", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),

    // 서버 오류
    INTERNAL_SERVER_ERROR("A-500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}