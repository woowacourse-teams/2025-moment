package moment.global.exception;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.stream.Collectors;
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

        if (errorCode == ErrorCode.INTERNAL_SERVER_ERROR) {
            log.error("InternalServiceError Occurred",
                    kv("errorCode", errorCode.name()),
                    kv("status", exception.getStatus()),
                    kv("errorMessage", exception.getMessage()),
                    exception
            );
        }
        if (errorCode != ErrorCode.INTERNAL_SERVER_ERROR) {
            log.warn("Handled MomentException",
                    kv("errorCode", errorCode.name()),
                    kv("status", exception.getStatus()),
                    kv("errorMessage", exception.getMessage())
            );
        }

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        return ResponseEntity.status(exception.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String validationDetails = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        log.warn("Validation Failed",
                kv("exception", exception.getClass().getSimpleName()),
                kv("errorMessage", exception.getMessage()),
                kv("details", validationDetails)
        );

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.REQUEST_INVALID);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("Illegal Argument Exception",
                kv("exception", exception.getClass().getSimpleName()),
                kv("errorMessage", exception.getMessage()),
                exception
        );

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled Exception Occurred",
                kv("exception", exception.getClass().getSimpleName()),
                kv("errorMessage", exception.getMessage()),
                exception
        );

        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
