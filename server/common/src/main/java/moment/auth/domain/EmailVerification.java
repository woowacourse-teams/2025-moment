package moment.auth.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;

@Getter
public class EmailVerification {

    private final String value;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiryTime;

    public EmailVerification(String value, LocalDateTime issuedAt, long expirySeconds) {
        this.value = value;
        this.issuedAt = issuedAt;
        this.expiryTime = issuedAt.plusSeconds(expirySeconds);
    }

    public boolean isCoolTime(long coolDownSeconds) {
        long secondsSinceLast = ChronoUnit.SECONDS.between(issuedAt, LocalDateTime.now());
        return secondsSinceLast < coolDownSeconds;
    }

    public boolean hasSameCode(String code) {
        return this.value.equals(code);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
