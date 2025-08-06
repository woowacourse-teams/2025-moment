package moment.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("mimi@icloud.com", "1234", "mimi", ProviderType.EMAIL);
    }

    @Test
    void 유저_생성에_성공한다() {
        // when & then
        assertThatCode(() -> new User("mimi@icloud.com", "1234", "mimi", ProviderType.EMAIL))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 이메일이_빈_값인_경우_예외가_발생한다(String email) {
        // when & then
        assertThatThrownBy(() -> new User(email, "password", "mimi", ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email이 null이거나 빈 값이어서는 안 됩니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"mimi", "mimi@", "mimi@.com", "mimi@com", "mimi@icloud", "mimi@icloud."})
    void 이메일_형식이_유효하지_않은_경우_예외가_발생한다(String email) {
        // when & then
        assertThatThrownBy(() -> new User(email, "password", "mimi", ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 이메일 형식입니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 비밀번호_형식이_유효하지_않은_경우_예외가_발생한다(String password) {
        // when & then
        assertThatThrownBy(() -> new User("mimi@icloud.com", password, "mimi", ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password가 null이거나 빈 값이어서는 안 됩니다.");
    }

    // todo 닉네임 형식 변경 시 다시 테스트 다시 살려야함
    @ParameterizedTest
    @CsvSource(value = {"m", "!mimi", "mimimim"})
    @Disabled
    void 닉네임_형식이_유효하지_않은_경우_예외가_발생한다(String nickname) {
        // when & then
        assertThatThrownBy(() -> new User("mimi@icloud.com", "password", nickname, ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 닉네임 형식입니다.");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void 닉네임_형식이_빈_값인_경우_예외가_발생한다(String nickname) {
        // when & then
        assertThatThrownBy(() -> new User("mimi@icloud.com", "password", nickname, ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("nickname이 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void 동일한_비밀번호를_입력받으면_참을_반환한다() {
        // given
        User user = new User("ekorea623@gmail.com", "12345", "drago", ProviderType.EMAIL);
        String password = "12345";

        // when & then
        assertThat(user.checkPassword(password)).isTrue();
    }

    @Test
    void 다른_비밀번호를_입력받으면_거짓을_반환한다() {
        // given
        User user = new User("ekorea623@gmail.com", "12345", "drago", ProviderType.EMAIL);
        String password = "abcdef";

        // when & then
        assertThat(user.checkPassword(password)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "0, 50, 50, METEOR",
            "50, 10, 60, ASTEROID",
            "50, 150, 200, COMET",
            "190, 10, 200, COMET"
    })
    void 포인트를_추가하고_레벨을_업데이트한다(int initialPoint, int pointToAdd, int expectedPoint, Level expectedLevel) {
        // given
        user.addPointAndUpdateLevel(initialPoint);

        // when
        user.addPointAndUpdateLevel(pointToAdd);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(expectedPoint);
        assertThat(user.getLevel()).isEqualTo(expectedLevel);
    }
}
