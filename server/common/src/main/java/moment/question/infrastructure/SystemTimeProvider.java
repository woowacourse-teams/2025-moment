package moment.question.infrastructure;

import java.time.LocalDateTime;
import moment.global.domain.TimeProvider;
import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
