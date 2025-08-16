package moment.user.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class LevelTest {

    @ParameterizedTest(name = "{0}포인트는 {1} 레벨이어야 한다")
    @CsvSource({
            "0, ASTEROID_WHITE",
            "5, ASTEROID_YELLOW",
            "10, ASTEROID_SKY",

            "20, METEOR_WHITE",
            "50, METEOR_YELLOW",
            "100, METEOR_SKY",

            "200, COMET_WHITE",
            "350, COMET_YELLOW",
            "700, COMET_SKY",

            "1200, ROCKY_PLANET_WHITE",
            "2000, ROCKY_PLANET_YELLOW",
            "4000, ROCKY_PLANET_SKY",

            "8000, GAS_GIANT_WHITE",
            "16000, GAS_GIANT_YELLOW",
            "32000, GAS_GIANT_SKY",
    })
    void 다양한_포인트_값에_따라_정확한_레벨을_반환해야_한다(int point, Level expectedLevel) {
        // when
        Level result = Level.getLevel(point);

        // then
        assertThat(result).isEqualTo(expectedLevel);
    }
}
