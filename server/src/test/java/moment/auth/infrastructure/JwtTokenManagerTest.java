package moment.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenManagerTest {

    private final String testSecretKey = "this-is-a-long-and-secure-secret-key-for-testing-moment-project";
    private final int testExpirationTime = 3600000;
    private final int testRefreshExpirationTime = 604800000;
    private JwtTokenManager jwtTokenManager;

    @BeforeEach
    void setUp() {
        jwtTokenManager = new JwtTokenManager(testExpirationTime, testSecretKey, testRefreshExpirationTime);
    }

    @Test
    void 사용자_ID와_이메일로_JWT_토큰을_정상적으로_생성한다() {
        // given
        Long userId = 100L;
        String email = "ekorea623@gmail.com";

        // when
        String token = jwtTokenManager.createAccessToken(userId, email);

        // then
        assertThat(token).isNotNull().isNotEmpty();

        SecretKeySpec key = new SecretKeySpec(testSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertAll(
                () -> assertThat(claims.getSubject()).isEqualTo(userId.toString()),
                () -> assertThat(claims.get("email", String.class)).isEqualTo(email)
        );
    }
}
