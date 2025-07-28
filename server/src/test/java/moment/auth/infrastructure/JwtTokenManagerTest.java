package moment.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenManagerTest {

    private JwtTokenManager jwtTokenManager;
    private final String testSecretKey = "this-is-a-long-and-secure-secret-key-for-testing-moment-project";
    private final int testExpirationTime = 3600000;

    @BeforeEach
    void setUp() {
        jwtTokenManager = new JwtTokenManager(testExpirationTime, testSecretKey);
    }

    @Test
    void 사용자_ID와_이메일로_JWT_토큰을_정상적으로_생성한다() {
        // given
        Long userId = 100L;
        String email = "ekorea623@gmail.com";

        // when
        String token = jwtTokenManager.createToken(userId, email);

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
