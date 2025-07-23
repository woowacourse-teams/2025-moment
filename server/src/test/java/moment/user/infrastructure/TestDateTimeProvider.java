package moment.user.infrastructure;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

@Component
@Primary
public class TestDateTimeProvider implements DateTimeProvider {

    private LocalDateTime now;

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.ofNullable(now);
    }

    public void setNow(LocalDateTime now) {
        this.now = now;
    }
}
