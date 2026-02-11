package moment.auth.dto.apple;

/**
 * Apple JWKS의 개별 공개키 정보
 * https://appleid.apple.com/auth/keys 응답의 keys 배열 요소
 */
public record ApplePublicKey(
        String kty,  // 키 타입 (RSA)
        String kid,  // 키 ID
        String use,  // 용도 (sig)
        String alg,  // 알고리즘 (RS256)
        String n,    // RSA modulus (Base64URL)
        String e     // RSA exponent (Base64URL)
) {
    /**
     * JWT 헤더의 kid, alg와 일치하는지 확인
     */
    public boolean matches(String targetKid, String targetAlg) {
        return this.kid.equals(targetKid) && this.alg.equals(targetAlg);
    }
}
