package moment.user.domain;

import java.util.Arrays;
import java.util.Comparator;
import lombok.Getter;

@Getter
public enum Level {

    METEOR(0),
    ASTEROID(60),
    COMET(200);

    private final int minPoints;

    Level(int minPoints) {
        this.minPoints = minPoints;
    }

    public static Level getLevel(Integer point) {
        return Arrays.stream(values())
                .filter(level -> point >= level.minPoints)
                .max(Comparator.comparingInt(filteredLevel -> filteredLevel.minPoints))
                .orElse(METEOR);
    }
}
