package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}
