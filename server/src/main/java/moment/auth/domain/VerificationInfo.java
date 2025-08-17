package moment.auth.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class VerificationInfo {

    private final String code;
    private final LocalDateTime expiryTime;
    private final LocalDateTime timestamp;

    public VerificationInfo(String code, LocalDateTime expiryTime) {
        this.code = code;
        this.expiryTime = expiryTime;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isCoolTime(long coolDownSeconds) {
        return this.timestamp
                .plusSeconds(coolDownSeconds)
                .isAfter(LocalDateTime.now());
    }

    public boolean hasSameCode(String code) {
        return this.code.equals(code);
    }
}
