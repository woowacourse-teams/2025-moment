package moment.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MomentException extends RuntimeException {

    private final ErrorCode errorCode;

    public MomentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
