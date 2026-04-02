package moment.global.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.global.domain.TimeProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    private final TimeProvider timeProvider;

    @GetMapping("/health")
    public ResponseEntity<Void> checkHealth() {
        log.info(timeProvider.now().toString());
        return ResponseEntity.ok().build();
    }
}
