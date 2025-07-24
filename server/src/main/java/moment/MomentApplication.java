package moment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MomentApplication {
    //    CI/CD TEST
    public static void main(String[] args) {
        SpringApplication.run(MomentApplication.class, args);
    }
}
