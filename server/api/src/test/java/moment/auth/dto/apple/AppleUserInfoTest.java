package moment.auth.dto.apple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AppleUserInfoTest {

    @Test
    void email이_있으면_해당_이메일을_반환한다() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234.0123", "user@icloud.com");

        String email = userInfo.resolveDisplayEmail();

        assertThat(email).isEqualTo("user@icloud.com");
    }

    @Test
    void email이_null이면_짧은_이메일을_생성한다() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234.0123", null);

        String email = userInfo.resolveDisplayEmail();

        assertThat(email).startsWith("apple_");
        assertThat(email).endsWith("@apple.app");
    }

    @Test
    void email이_빈_문자열이면_짧은_이메일을_생성한다() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234.0123", "");

        String email = userInfo.resolveDisplayEmail();

        assertThat(email).startsWith("apple_");
        assertThat(email).endsWith("@apple.app");
    }

    @Test
    void 생성된_이메일은_유효한_이메일_형식이다() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234.0123", null);

        String email = userInfo.resolveDisplayEmail();

        assertThat(email).matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    @Test
    void 동일한_sub에_대해_항상_같은_이메일을_생성한다() {
        String sub = "001234.abcd1234efgh5678.0123";
        AppleUserInfo userInfo1 = new AppleUserInfo(sub, null);
        AppleUserInfo userInfo2 = new AppleUserInfo(sub, null);

        assertThat(userInfo1.resolveDisplayEmail()).isEqualTo(userInfo2.resolveDisplayEmail());
    }

    @Test
    void toLegacyEmail은_sub_apple_user_형태를_반환한다() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234efgh5678.0123", null);

        String legacyEmail = userInfo.toLegacyEmail();

        assertThat(legacyEmail).isEqualTo("001234.abcd1234efgh5678.0123@apple.user");
    }
}
