package moment.question.infrastructure;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import moment.global.domain.TimeProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
