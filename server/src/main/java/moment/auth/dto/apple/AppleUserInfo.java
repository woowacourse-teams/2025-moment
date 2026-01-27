package moment.auth.dto.apple;

/**
 * Apple Identity Token에서 추출한 사용자 정보
 */
public record AppleUserInfo(
        String sub  // Apple 사용자 고유 ID (필수)
) {
    /**
     * sub 기반 이메일 생성
     * 예: 001234.abcd1234.0123 -> 001234.abcd1234.0123@apple.user
     */
    public String toAppleEmail() {
        return sub + "@apple.user";
    }
}
