package moment.support;

import java.time.LocalDateTime;
import moment.global.domain.TimeProvider;

public class FakeTimeProvider implements TimeProvider {

    private LocalDateTime currentTime;

    public FakeTimeProvider(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public void changeTime(LocalDateTime newTime) {
        this.currentTime = newTime;
    }

    @Override
    public LocalDateTime now() {
        return currentTime;
    }
}
