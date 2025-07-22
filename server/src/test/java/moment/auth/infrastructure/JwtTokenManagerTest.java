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

    @Test
    void 만료된_토큰을_검증하면_예외가_발생한다() throws InterruptedException {
        // given
        JwtTokenManager expiredTokenManager = new JwtTokenManager(1, testSecretKey);
        String expiredToken = expiredTokenManager.createToken(1L, "expired@moment.com");

        // when
        Thread.sleep(5);

        // then
        assertThatThrownBy(() -> jwtTokenManager.extractClaims(expiredToken))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void 잘못된_시크릿키로_생성된_토큰을_검증하면_예외가_발생한다() {
        // given
        String invalidSecretKey = "this-is-a-completely-different-secret-key-for-moment";
        JwtTokenManager invalidKeyManager = new JwtTokenManager(testExpirationTime, invalidSecretKey);

        // when
        String invalidToken = invalidKeyManager.createToken(1L, "user@moment.com");

        // then
        assertThatThrownBy(() -> jwtTokenManager.extractClaims(invalidToken))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_NOT_SIGNED);
    }
}
