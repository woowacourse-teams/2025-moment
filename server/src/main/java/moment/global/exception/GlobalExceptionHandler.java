package moment.global.exception;

import lombok.extern.slf4j.Slf4j;
import moment.global.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MomentException.class)
    public ResponseEntity<ErrorResponse> handleMomentException(MomentException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        if(errorCode == ErrorCode.INTERNAL_SERVER_ERROR) {
            log.error(exception.getMessage(), exception);
        }
        if(errorCode != ErrorCode.INTERNAL_SERVER_ERROR) {
            log.warn(exception.getMessage(), exception);
        }

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    // TODO : 임시로 작성, 검증 예외 어떻게 반환할지 다같이 고민해보기
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.REQUEST_INVALID);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error(exception.getMessage(), exception);

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
