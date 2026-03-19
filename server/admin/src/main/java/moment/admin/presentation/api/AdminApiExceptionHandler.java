package moment.admin.presentation.api;

import lombok.extern.slf4j.Slf4j;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(basePackages = "moment.admin.presentation.api")
public class AdminApiExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<AdminErrorResponse> handleAdminException(AdminException e) {
        AdminErrorCode errorCode = e.getErrorCode();
        log.warn("Admin API Exception: code={}, message={}",
                 errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
            .status(e.getStatus())
            .body(AdminErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AdminErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AdminErrorResponse.from(AdminErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<AdminErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AdminErrorResponse.from(AdminErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AdminErrorResponse> handleException(Exception e) {
        log.error("Unexpected error in Admin API", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AdminErrorResponse.from(AdminErrorCode.INTERNAL_SERVER_ERROR));
    }
}
