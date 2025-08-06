package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @CsvSource(value = {"m", "!mimi", "mimimim"})
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

    @Test
    void 유저_생성_시_초기값이_올바르게_설정된다() {
        // when
        User newUser = new User("test@example.com", "password123", "testuser", ProviderType.EMAIL);

        // then
        assertThat(newUser.getEmail()).isEqualTo("test@example.com");
        assertThat(newUser.getNickname()).isEqualTo("testuser");
        assertThat(newUser.getProviderType()).isEqualTo(ProviderType.EMAIL);
        assertThat(newUser.getCurrentPoint()).isEqualTo(0);
        assertThat(newUser.getLevel()).isEqualTo(Level.METEOR);
    }

    @Test
    void 구글_프로바이더로_유저_생성에_성공한다() {
        // when & then
        assertThatCode(() -> new User("google@example.com", "password", "social1", ProviderType.GOOGLE))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_ProviderType으로_유저_생성이_가능하다() {
        // given & when & then
        for (ProviderType providerType : ProviderType.values()) {
            assertThatCode(() -> new User("test@example.com", "password123", "testuser", providerType))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void ProviderType이_null인_경우_생성자에서_NPE가_발생할_수_있다() {


    @ParameterizedTest
    @CsvSource({
        "password123, password123, true",
        "Password123, password123, false",
        "password123, Password123, false",
        "12345, 12345, true",
        "한글비밀번호, 한글비밀번호, true",
        "special!@#$%^&*(), special!@#$%^&*(), true"
    })
    void 비밀번호_체크_다양한_케이스_테스트(String userPassword, String inputPassword, boolean expected) {
        // given
        User testUser = new User("test@example.com", userPassword, "testuser", ProviderType.EMAIL);

        // when & then
        assertThat(testUser.checkPassword(inputPassword)).isEqualTo(expected);
    }

    @Test
    void 비밀번호_체크_시_null_입력하면_NPE가_발생한다() {
        // when & then
        assertThatThrownBy(() -> user.checkPassword(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 비밀번호_체크_시_빈_문자열_입력하면_거짓을_반환한다() {
        // when & then
        assertThat(user.checkPassword("")).isFalse();
        assertThat(user.checkPassword(" ")).isFalse();
        assertThat(user.checkPassword("   ")).isFalse();
    }

    @Test
    void 매우_긴_비밀번호도_정확히_체크한다() {


    @ParameterizedTest
    @CsvSource({
        "valid@example.com",
        "user.email@domain.co.kr",
        "test+label@gmail.com",
        "123@numeric-domain.com",
        "hyphen-email@test-domain.org",
        "underscore_email@test_domain.net",
        "a@b.co",
        "very.long.email.address@very.long.domain.name.com",
        "numbers123@domain456.net"
    })
    void 다양한_유효한_이메일_형식으로_유저_생성에_성공한다(String email) {
        // when & then
        assertThatCode(() -> new User(email, "password", "nick12", ProviderType.EMAIL))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
        "@example.com",
        "user@@domain.com",
        "user@domain@com",
        ".user@domain.com",
        "user.@domain.com",
        "user@domain..com",
        "user name@domain.com",
        "user@도메인.com",
        "user@domain.",
        "@",
        "user@",
        "@domain.com",
        "user@domain",
        "user@.domain.com"
    })
    void 추가_이메일_형식_검증_테스트(String email) {


    @ParameterizedTest
    @CsvSource({
        "ab",
        "abc",
        "test",
        "user12",
        "한글닉",
        "한글123",
        "123456",
        "가나다라마바",
        "abc가나",
        "123한글"
    })
    void 유효한_닉네임으로_유저_생성에_성공한다(String nickname) {
        // when & then
        assertThatCode(() -> new User("test@example.com", "password", nickname, ProviderType.EMAIL))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
        "a",
        "1234567",
        "toolong",
        "!invalid",
        "@nickname",
        "#hash",
        "%percent",
        "nick name",
        "nick.name",
        "nick-name",
        "nick_name",
        "한글이름이너무길어",
        "special!@#",
        "with spaces",
        "한글 공백"
    })
    void 추가_닉네임_형식_검증_테스트(String nickname) {


    @ParameterizedTest
    @CsvSource({
        "0, METEOR",
        "59, METEOR",
        "60, ASTEROID",
        "100, ASTEROID",
        "199, ASTEROID",
        "200, COMET",
        "500, COMET",
        "1000, COMET"
    })
    void 포인트에_따른_정확한_레벨_결정_테스트(int point, Level expectedLevel) {
        // when
        user.addPointAndUpdateLevel(point);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(point);
        assertThat(user.getLevel()).isEqualTo(expectedLevel);
    }

    @Test
    void 레벨_경계값_테스트() {
        // METEOR to ASTEROID 경계
        user.addPointAndUpdateLevel(59);
        assertThat(user.getLevel()).isEqualTo(Level.METEOR);
        
        user.addPointAndUpdateLevel(1); // 60 total
        assertThat(user.getLevel()).isEqualTo(Level.ASTEROID);
        
        // ASTEROID to COMET 경계
        User user2 = new User("test2@example.com", "pass", "test2", ProviderType.EMAIL);
        user2.addPointAndUpdateLevel(199);
        assertThat(user2.getLevel()).isEqualTo(Level.ASTEROID);
        
        user2.addPointAndUpdateLevel(1); // 200 total
        assertThat(user2.getLevel()).isEqualTo(Level.COMET);
    }

    @Test
    void 매우_큰_포인트_누적_테스트() {
        // given
        int largePoint = 1000000;

        // when
        user.addPointAndUpdateLevel(largePoint);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(largePoint);
        assertThat(user.getLevel()).isEqualTo(Level.COMET);
    }

    @Test
    void 포인트_여러번_추가_시_누적되고_레벨이_정확히_업데이트된다() {
        // when
        user.addPointAndUpdateLevel(30); // 30, METEOR
        assertThat(user.getLevel()).isEqualTo(Level.METEOR);
        
        user.addPointAndUpdateLevel(35); // 65, ASTEROID
        assertThat(user.getLevel()).isEqualTo(Level.ASTEROID);
        
        user.addPointAndUpdateLevel(140); // 205, COMET
        assertThat(user.getLevel()).isEqualTo(Level.COMET);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(205);
        assertThat(user.getLevel()).isEqualTo(Level.COMET);
    }

    @Test
    void 제로_포인트_추가는_변화없음을_확인한다() {


    @Test
    void getter_메서드들이_올바른_값을_반환한다() {
        // given
        String email = "getter@test.com";
        String nickname = "getter";
        ProviderType providerType = ProviderType.EMAIL;
        
        User testUser = new User(email, "password", nickname, providerType);
        testUser.addPointAndUpdateLevel(150);

        // when & then
        assertThat(testUser.getEmail()).isEqualTo(email);
        assertThat(testUser.getNickname()).isEqualTo(nickname);
        assertThat(testUser.getProviderType()).isEqualTo(providerType);
        assertThat(testUser.getCurrentPoint()).isEqualTo(150);
        assertThat(testUser.getLevel()).isEqualTo(Level.ASTEROID);
    }

    @Test
    void equals는_id를_기준으로_동작한다() {
        // given
        User user1 = new User("test@example.com", "password", "test1", ProviderType.EMAIL);
        User user2 = new User("different@example.com", "different", "test2", ProviderType.GOOGLE);
        User user3 = new User("test@example.com", "password", "test1", ProviderType.EMAIL);

        // when & then - 모든 User 객체는 id가 null이므로 동등하지 않음
        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    void toString_메서드가_주요_정보를_포함한다() {
        // when
        String result = user.toString();

        // then
        assertThat(result).contains("User");
        assertThat(result).contains("mimi@icloud.com");
        assertThat(result).contains("mimi");
        assertThat(result).contains("EMAIL");
    }

    @Test
    void 다른_ProviderType의_toString_테스트() {


    @Test
    void 매우_긴_입력값들로_유저_생성_테스트() {
        // given
        String longEmail = "a".repeat(50) + "@" + "b".repeat(50) + ".com";
        String longPassword = "password" + "x".repeat(1000);
        
        // when & then
        assertThatCode(() -> new User(longEmail, longPassword, "test12", ProviderType.EMAIL))
                .doesNotThrowAnyException();
    }

    @Test
    void 특수문자가_포함된_비밀번호_테스트() {
        // given
        String specialPassword = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        // when & then
        assertThatCode(() -> new User("test@example.com", specialPassword, "test12", ProviderType.EMAIL))
                .doesNotThrowAnyException();
                
        User testUser = new User("test@example.com", specialPassword, "test12", ProviderType.EMAIL);
        assertThat(testUser.checkPassword(specialPassword)).isTrue();
        assertThat(testUser.checkPassword("different")).isFalse();
    }

    @Test
    void 최대_포인트_오버플로우_방지_테스트() {
        // given
        user.addPointAndUpdateLevel(Integer.MAX_VALUE - 100);
        
        // when & then - 오버플로우가 발생하지 않아야 함
        assertThatCode(() -> user.addPointAndUpdateLevel(50))
                .doesNotThrowAnyException();
                
        // 결과 검증
        assertThat(user.getCurrentPoint()).isEqualTo(Integer.MAX_VALUE - 50);
        assertThat(user.getLevel()).isEqualTo(Level.COMET);
    }

    @Test
    void 음수_포인트_추가_시도_테스트() {
        // given
        int initialPoint = user.getCurrentPoint();
        
        // when & then - 현재 구현에서는 음수 포인트도 허용됨
        assertThatCode(() -> user.addPointAndUpdateLevel(-10))
                .doesNotThrowAnyException();
                
        assertThat(user.getCurrentPoint()).isEqualTo(initialPoint - 10);
    }
        // given
        User googleUser = new User("google@test.com", "pass", "google", ProviderType.GOOGLE);
        
        // when
        String result = googleUser.toString();
        
        // then
        assertThat(result).contains("GOOGLE");
        assertThat(result).contains("google@test.com");
        assertThat(result).contains("google");
    }
        // given
        Level initialLevel = user.getLevel();
        int initialPoint = user.getCurrentPoint();
        
        // when
        user.addPointAndUpdateLevel(0);
        
        // then
        assertThat(user.getCurrentPoint()).isEqualTo(initialPoint);
        assertThat(user.getLevel()).isEqualTo(initialLevel);
    }
        // when & then
        assertThatThrownBy(() -> new User("test@example.com", "password", nickname, ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 닉네임 형식입니다.");
    }
        // when & then
        assertThatThrownBy(() -> new User(email, "password", "nick12", ProviderType.EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 이메일 형식입니다.");
    }
        // given
        String longPassword = "a".repeat(1000);
        User testUser = new User("test@example.com", longPassword, "testuser", ProviderType.EMAIL);

        // when & then
        assertThat(testUser.checkPassword(longPassword)).isTrue();
        assertThat(testUser.checkPassword(longPassword + "x")).isFalse();
    }
        // when & then
        assertThatThrownBy(() -> new User("test@example.com", "password", "nickname", null))
                .isInstanceOf(NullPointerException.class);
    }
        // given
        user.addPointAndUpdateLevel(initialPoint);

        // when
        user.addPointAndUpdateLevel(pointToAdd);

        // then
        assertThat(user.getCurrentPoint()).isEqualTo(expectedPoint);
        assertThat(user.getLevel()).isEqualTo(expectedLevel);
    }
}
