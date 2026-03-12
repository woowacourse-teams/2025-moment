package moment.global.domain;

import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
}
