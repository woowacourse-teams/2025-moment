package moment.auth.dto.apple;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Apple Identity Token에서 추출한 사용자 정보
 */
public record AppleUserInfo(
        String sub,   // Apple 사용자 고유 ID (필수)
        String email  // Apple 이메일 (nullable - 사용자가 이메일 공유를 선택한 경우에만 존재)
) {
    /**
     * 표시용 이메일 결정
     * - email이 있으면 그대로 사용
     * - 없으면 sub의 MD5 해시 앞 8자로 짧은 이메일 생성
     */
    public String resolveDisplayEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        String hash = md5(sub).substring(0, 8);
        return "apple_" + hash + "@apple.app";
    }

    /**
     * 기존 sub@apple.user 형태의 이메일 생성 (backward compatibility 조회용)
     */
    public String toLegacyEmail() {
        return sub + "@apple.user";
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
