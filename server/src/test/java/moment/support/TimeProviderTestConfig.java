package moment.support;

import java.time.LocalDateTime;
import moment.global.domain.TimeProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TimeProviderTestConfig {

    @Bean
    @Primary
    public TimeProvider fakeTimeProvider() {
        return new FakeTimeProvider(LocalDateTime.of(2026, 3, 10, 10, 0));
    }
}
