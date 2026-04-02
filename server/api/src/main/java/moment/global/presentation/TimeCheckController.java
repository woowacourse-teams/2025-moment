package moment.global.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.global.domain.TimeProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TimeCheckController {

    private final TimeProvider timeProvider;

    @GetMapping("/api/test/time")
    public String checkTime() {
        return "Server Time: " + timeProvider.now().toString();
    }
}
