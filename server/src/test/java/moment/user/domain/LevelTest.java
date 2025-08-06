package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
class LevelTest {

    @ParameterizedTest(name = "{0}포인트는 {1} 레벨이어야 한다")
    @CsvSource({
            "0, METEOR",
            "30, METEOR",
            "59, METEOR",

            "60, ASTEROID",
            "150, ASTEROID",
            "199, ASTEROID",

            "200, COMET",
            "5000, COMET"
    })
    void 다양한_포인트_값에_따라_정확한_레벨을_반환해야_한다(int point, Level expectedLevel) {
        // when
        Level result = Level.getLevel(point);

        // then
        assertThat(result).isEqualTo(expectedLevel);
    }

    @Test
    void 경계값_테스트_METEOR에서_ASTEROID로_변환() {
        // given
        int meterorMaxPoint = 59;
        int asteroidMinPoint = 60;

        // when & then
        assertThat(Level.getLevel(meterorMaxPoint)).isEqualTo(Level.METEOR);
        assertThat(Level.getLevel(asteroidMinPoint)).isEqualTo(Level.ASTEROID);
    }

    @Test
    void 경계값_테스트_ASTEROID에서_COMET으로_변환() {
        // given
        int asteroidMaxPoint = 199;
        int cometMinPoint = 200;

        // when & then
        assertThat(Level.getLevel(asteroidMaxPoint)).isEqualTo(Level.ASTEROID);
        assertThat(Level.getLevel(cometMinPoint)).isEqualTo(Level.COMET);
    }

    @ParameterizedTest(name = "음수 포인트 {0}에 대해 예외가 발생해야 한다")
    @ValueSource(ints = {-1, -10, -100, Integer.MIN_VALUE})
    void 음수_포인트에_대해_예외를_발생시켜야_한다(int negativePoint) {
        // when & then
        assertThatThrownBy(() -> Level.getLevel(negativePoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트는 0 이상이어야 합니다");
    }

    @Test
    void 매우_큰_포인트_값도_COMET_레벨을_반환해야_한다() {
        // given
        int veryLargePoint = Integer.MAX_VALUE;

        // when
        Level result = Level.getLevel(veryLargePoint);

        // then
        assertThat(result).isEqualTo(Level.COMET);
    }

    @Test
    void Level_enum의_모든_값들이_존재해야_한다() {
        // when
        Level[] levels = Level.values();

        // then
        assertThat(levels).containsExactly(Level.METEOR, Level.ASTEROID, Level.COMET);
        assertThat(levels).hasSize(3);
    }

    @Test
    void Level_enum의_valueOf_메서드가_정상_동작해야_한다() {
        // when & then
        assertThat(Level.valueOf("METEOR")).isEqualTo(Level.METEOR);
        assertThat(Level.valueOf("ASTEROID")).isEqualTo(Level.ASTEROID);
        assertThat(Level.valueOf("COMET")).isEqualTo(Level.COMET);
    }

    @Test
    void 존재하지_않는_Level_이름으로_valueOf_호출시_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> Level.valueOf("INVALID_LEVEL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void null_값으로_valueOf_호출시_예외가_발생해야_한다() {
        // when & then
        assertThatThrownBy(() -> Level.valueOf(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void Level_enum의_name_메서드가_정상_동작해야_한다() {
        // when & then
        assertThat(Level.METEOR.name()).isEqualTo("METEOR");
        assertThat(Level.ASTEROID.name()).isEqualTo("ASTEROID");
        assertThat(Level.COMET.name()).isEqualTo("COMET");
    }

    @Test
    void Level_enum의_ordinal_메서드가_정상_동작해야_한다() {
        // when & then
        assertThat(Level.METEOR.ordinal()).isEqualTo(0);
        assertThat(Level.ASTEROID.ordinal()).isEqualTo(1);
        assertThat(Level.COMET.ordinal()).isEqualTo(2);
    }

    @Test
    void Level_간_compareTo_메서드가_정상_동작해야_한다() {
        // when & then
        assertThat(Level.METEOR.compareTo(Level.ASTEROID)).isLessThan(0);
        assertThat(Level.ASTEROID.compareTo(Level.COMET)).isLessThan(0);
        assertThat(Level.COMET.compareTo(Level.METEOR)).isGreaterThan(0);
        assertThat(Level.METEOR.compareTo(Level.METEOR)).isEqualTo(0);
    }

    @Test
    void Level_enum의_toString_메서드가_정상_동작해야_한다() {
        // when & then
        assertThat(Level.METEOR.toString()).isEqualTo("METEOR");
        assertThat(Level.ASTEROID.toString()).isEqualTo("ASTEROID");
        assertThat(Level.COMET.toString()).isEqualTo("COMET");
    }

    @Test
    void 모든_레벨에_대해_getLevel_메서드가_일관되게_동작해야_한다() {
        // given
        int[] meteorPoints = {0, 1, 30, 59};
        int[] asteroidPoints = {60, 100, 150, 199};
        int[] cometPoints = {200, 500, 1000, 10000};

        // when & then
        for (int point : meteorPoints) {
            assertThat(Level.getLevel(point))
                    .as("Point %d should be METEOR", point)
                    .isEqualTo(Level.METEOR);
        }

        for (int point : asteroidPoints) {
            assertThat(Level.getLevel(point))
                    .as("Point %d should be ASTEROID", point)
                    .isEqualTo(Level.ASTEROID);
        }

        for (int point : cometPoints) {
            assertThat(Level.getLevel(point))
                    .as("Point %d should be COMET", point)
                    .isEqualTo(Level.COMET);
        }
    }

    @Test
    void 레벨_등급_순서가_올바른지_확인해야_한다() {
        // when & then
        assertThat(Level.METEOR.ordinal()).isLessThan(Level.ASTEROID.ordinal());
        assertThat(Level.ASTEROID.ordinal()).isLessThan(Level.COMET.ordinal());
    }

    @ParameterizedTest(name = "랜덤한 유효한 포인트 {0}에 대해 null이 아닌 레벨을 반환해야 한다")
    @ValueSource(ints = {1, 25, 61, 99, 201, 999, 2500})
    void 유효한_포인트에_대해_null이_아닌_레벨을_반환해야_한다(int validPoint) {
        // when
        Level result = Level.getLevel(validPoint);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isIn(Level.METEOR, Level.ASTEROID, Level.COMET);
    }
}
