package moment.auth.dto.apple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApplePublicKeysTest {

    @Test
    @DisplayName("일치하는 키가 있으면 해당 키를 반환한다")
    void findMatchingKey_일치하는_키가_있으면_반환() {
        ApplePublicKey key1 = new ApplePublicKey("RSA", "key1", "sig", "RS256", "n1", "e1");
        ApplePublicKey key2 = new ApplePublicKey("RSA", "key2", "sig", "RS256", "n2", "e2");
        ApplePublicKeys keys = new ApplePublicKeys(List.of(key1, key2));

        Optional<ApplePublicKey> result = keys.findMatchingKey("key2", "RS256");

        assertThat(result).isPresent();
        assertThat(result.get().kid()).isEqualTo("key2");
    }

    @Test
    @DisplayName("일치하는 키가 없으면 empty를 반환한다")
    void findMatchingKey_일치하는_키가_없으면_empty() {
        ApplePublicKey key1 = new ApplePublicKey("RSA", "key1", "sig", "RS256", "n1", "e1");
        ApplePublicKeys keys = new ApplePublicKeys(List.of(key1));

        Optional<ApplePublicKey> result = keys.findMatchingKey("nonexistent", "RS256");

        assertThat(result).isEmpty();
    }
}
