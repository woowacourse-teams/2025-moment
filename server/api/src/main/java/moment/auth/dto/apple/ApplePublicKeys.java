package moment.auth.dto.apple;

import java.util.List;
import java.util.Optional;

/**
 * Apple JWKS 응답
 * https://appleid.apple.com/auth/keys
 */
public record ApplePublicKeys(
        List<ApplePublicKey> keys
) {
    /**
     * kid와 alg가 일치하는 공개키 찾기
     */
    public Optional<ApplePublicKey> findMatchingKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.matches(kid, alg))
                .findFirst();
    }
}
