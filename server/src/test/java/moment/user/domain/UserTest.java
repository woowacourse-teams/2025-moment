package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.createUser();
    }

    @Test
    void 유저_생성에_성공한다() {
        // when & then
        assertThatCode(UserFixture::createUser)
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 이메일이_빈_값인_경우_예외가_발생한다(String email) {
        // when & then
        assertThatThrownBy(() -> UserFixture.createUserByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email이 null이거나 빈 값이어서는 안 됩니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"mimi", "mimi@", "mimi@.com", "mimi@com", "mimi@icloud", "mimi@icloud."})
    void 이메일_형식이_유효하지_않은_경우_예외가_발생한다(String email) {
        // when & then
        assertThatThrownBy(() -> UserFixture.createUserByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 이메일 형식입니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 비밀번호_형식이_유효하지_않은_경우_예외가_발생한다(String password) {
        // when & then
        assertThatThrownBy(() -> UserFixture.createUserByPassword(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password가 null이거나 빈 값이어서는 안 됩니다.");
    }

    // todo 닉네임 형식 변경 시 다시 테스트 다시 살려야함
    @ParameterizedTest
    @CsvSource(value = {"m", "!mimi", "mimimim"})
    @Disabled
    void 닉네임_형식이_유효하지_않은_경우_예외가_발생한다(String nickname) {
        // when & then
        assertThatThrownBy(() -> UserFixture.createUserByNickname(nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 닉네임 형식입니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 닉네임_형식이_빈_값인_경우_예외가_발생한다(String nickname) {
        // when & then
        assertThatThrownBy(() -> UserFixture.createUserByNickname(nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nickname이 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void 동일한_비밀번호를_입력받으면_참을_반환한다() {
        // given
        User user = UserFixture.createUser();
        String password = user.getPassword();

        // when & then
        assertThat(user.checkPassword(password)).isTrue();
    }

    @Test
    void 다른_비밀번호를_입력받으면_거짓을_반환한다() {
        // given
        User user = UserFixture.createUser();
        String password = UserFixture.createUser().getPassword() + "test";

        // when & then
        assertThat(user.checkPassword(password)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "0, 4, 4, ASTEROID_WHITE",
            "4, 1, 5, ASTEROID_YELLOW",

            "5, 4, 9, ASTEROID_YELLOW",
            "9, 1, 10, ASTEROID_SKY",

            "10, 9, 19, ASTEROID_SKY",
            "19, 1, 20, METEOR_WHITE",

            "20, 29, 49, METEOR_WHITE",
            "49, 1, 50, METEOR_YELLOW",

            "50, 49, 99, METEOR_YELLOW",
            "99, 1, 100, METEOR_SKY",

            "100, 99, 199, METEOR_SKY",
            "199, 1, 200, COMET_WHITE",

            "200, 149, 349, COMET_WHITE",
            "349, 1, 350, COMET_YELLOW",

            "350, 349, 699, COMET_YELLOW",
            "699, 1, 700, COMET_SKY",

            "700, 499, 1199, COMET_SKY",
            "1199, 1, 1200, ROCKY_PLANET_WHITE",

            "1200, 799, 1999, ROCKY_PLANET_WHITE",
            "1999, 1, 2000, ROCKY_PLANET_YELLOW",

            "2000, 1999, 3999, ROCKY_PLANET_YELLOW",
            "3999, 1, 4000, ROCKY_PLANET_SKY",

            "4000, 3999, 7999, ROCKY_PLANET_SKY",
            "7999, 1, 8000, GAS_GIANT_WHITE",

            "8000, 7999, 15999, GAS_GIANT_WHITE",
            "15999, 1, 16000, GAS_GIANT_YELLOW",

            "16000, 15999, 31999, GAS_GIANT_YELLOW",
            "31999, 1, 32000, GAS_GIANT_SKY",
    })
    void 포인트를_추가하고_레벨을_업데이트한다(int initialPoint, int pointToAdd, int expectedPoint, Level expectedLevel) {
        // given
        user.addStarAndUpdateLevel(initialPoint);

        // when
        user.addStarAndUpdateLevel(pointToAdd);

        // then
        assertThat(user.getAvailableStar()).isEqualTo(expectedPoint);
        assertThat(user.getLevel()).isEqualTo(expectedLevel);
    }
}
