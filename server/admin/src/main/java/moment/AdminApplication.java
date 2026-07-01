package moment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AdminApplication {
    // admin 배포 트리거
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
