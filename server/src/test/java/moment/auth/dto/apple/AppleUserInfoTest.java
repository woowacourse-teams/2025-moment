package moment.auth.dto.apple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppleUserInfoTest {

    @Test
    @DisplayName("sub 기반 이메일을 생성한다")
    void toAppleEmail_sub_기반_이메일_생성() {
        AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234efgh5678.0123");

        String email = userInfo.toAppleEmail();

        assertThat(email).isEqualTo("001234.abcd1234efgh5678.0123@apple.user");
    }
}
