package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.config.TestTags;
import moment.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag(TestTags.UNIT)
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
}
