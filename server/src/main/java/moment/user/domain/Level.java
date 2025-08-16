package moment.user.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Level {
    ASTEROID_WHITE(0, 4),
    ASTEROID_YELLOW(5, 9),
    ASTEROID_SKY(10, 19),
    METEOR_WHITE(20, 49),
    METEOR_YELLOW(50, 99),
    METEOR_SKY(100, 199),
    COMET_WHITE(200, 349),
    COMET_YELLOW(350, 699),
    COMET_SKY(700, 1199),
    ROCKY_PLANET_WHITE(1200, 1999),
    ROCKY_PLANET_YELLOW(2000, 3999),
    ROCKY_PLANET_SKY(4000, 7999),
    GAS_GIANT_WHITE(8000, 15999),
    GAS_GIANT_YELLOW(16000, 31999),
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
        return point >= minPoints && point <= maxPoints;
    }

    public int getNextLevelRequiredStars() {
        return maxPoints + 1;
    }
}
