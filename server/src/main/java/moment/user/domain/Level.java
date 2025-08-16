package moment.user.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Level {
    ASTEROID_WHITE(0, 5),
    ASTEROID_YELLOW(5, 10),
    ASTEROID_SKY(10, 20),
    METEOR_WHITE(20, 50),
    METEOR_YELLOW(50, 100),
    METEOR_SKY(100, 200),
    COMET_WHITE(200, 350),
    COMET_YELLOW(350, 700),
    COMET_SKY(700, 1200),
    ROCKY_PLANET_WHITE(1200, 2000),
    ROCKY_PLANET_YELLOW(2000, 4000),
    ROCKY_PLANET_SKY(4000, 8000),
    GAS_GIANT_WHITE(8000, 16000),
    GAS_GIANT_YELLOW(16000, 32000),
    GAS_GIANT_SKY(32000, Integer.MAX_VALUE),
    ;

    private final int minPoints;
    private final int maxPoints;

    Level(int minPoints, int maxPoints) {
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }

    public static Level getLevel(Integer point) {
        return Arrays.stream(values())
                .filter(level -> level.isMatch(point))
                .findAny().orElseThrow(() -> new IllegalArgumentException("해당하는 레벨을 찾을 수 없습니다."));
    }

    public boolean isMatch(Integer point) {
        return point >= minPoints && point < maxPoints;
    }
}
