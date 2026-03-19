package moment.auth.dto.apple;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplePublicKeyTest {

    @Test
    @DisplayName("kid와 alg가 일치하면 true를 반환한다")
    void matches_kid와_alg가_일치하면_true() {
        ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");

        assertThat(key.matches("ABC123", "RS256")).isTrue();
    }

    @Test
    @DisplayName("kid가 다르면 false를 반환한다")
    void matches_kid가_다르면_false() {
        ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");

        assertThat(key.matches("DIFFERENT", "RS256")).isFalse();
    }

    @Test
    @DisplayName("alg가 다르면 false를 반환한다")
    void matches_alg가_다르면_false() {
        ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");

        assertThat(key.matches("ABC123", "RS512")).isFalse();
    }
}
