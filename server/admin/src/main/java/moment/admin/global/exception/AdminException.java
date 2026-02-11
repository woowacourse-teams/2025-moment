package moment.admin.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AdminException extends RuntimeException {
    private final AdminErrorCode errorCode;

    public AdminException(AdminErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
