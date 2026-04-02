package moment.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TimeConfig {

    @Bean
    @Primary
    public Clock seoulClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
