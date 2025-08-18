package moment.auth.infrastructure;

import static io.jsonwebtoken.Jwts.SIG.HS256;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import moment.auth.application.TokenManager;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.dto.request.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenManager implements TokenManager {

    private final int accessTokenExpirationTime;
    private final int refreshTokenExpirationTime;
    private final String accessSecretKey;
    private final String refreshSecretKey;

    public JwtTokenManager(
            @Value("${expiration.access-token-time}") int ACCESS_TOKEN_EXPIRATION_TIME,
            @Value("${expiration.refresh-token-time}") int REFRESH_TOKEN_EXPIRATION_TIME,
            @Value("${jwt.secret.access_key}") String ACCESS_SECRET_KEY,
            @Value("${jwt.secret.refresh_key}") String REFRESH_SECRET_KEY
    ) {
        this.accessTokenExpirationTime = ACCESS_TOKEN_EXPIRATION_TIME;
        this.refreshTokenExpirationTime = REFRESH_TOKEN_EXPIRATION_TIME;
        this.accessSecretKey = ACCESS_SECRET_KEY;
        this.refreshSecretKey = REFRESH_SECRET_KEY;
    }

    @Override
    public String createAccessToken(Long id, String email) {
        SecretKeySpec key = new SecretKeySpec(accessSecretKey.getBytes(), "HmacSHA256");

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .subject(id.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .signWith(key, HS256)
                .compact();
    }

    @Override
    public String createRefreshToken(Long id, String email) {
        SecretKeySpec key = new SecretKeySpec(refreshSecretKey.getBytes(), "HmacSHA256");

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .subject(id.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .signWith(key, HS256)
                .compact();
    }

    public Date getExpirationTimeFromToken(String token) {
        Jws<Claims> verifiedJwt = Jwts.parser()
                .verifyWith(new SecretKeySpec(refreshSecretKey.getBytes(), "HmacSHA256"))
                .build()
                .parseSignedClaims(token);

        return verifiedJwt.getPayload().getExpiration();
    }

    public Date getIssuedAtFromToken(String token) {
        Jws<Claims> verifiedJwt = Jwts.parser()
                .verifyWith(new SecretKeySpec(refreshSecretKey.getBytes(), "HmacSHA256"))
                .build()
                .parseSignedClaims(token);

        return verifiedJwt.getPayload().getIssuedAt();
    }

    @Override
    public Authentication extractAuthentication(String token) {
        Long id = handleTokenException(token, this::extractIdFromToken);

        return Authentication.from(id);
    }

    private <T> T handleTokenException(String token, Function<String, T> function) {
        try {
            return function.apply(token);
        } catch (MalformedJwtException malformedJwtException) {
            throw new MomentException(ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new MomentException(ErrorCode.TOKEN_EXPIRED);
        } catch (IllegalArgumentException argumentException) {
            throw new MomentException(ErrorCode.TOKEN_EMPTY);
        } catch (SignatureException signatureException) {
            throw new MomentException(ErrorCode.TOKEN_NOT_SIGNED);
        } catch (JwtException jwtException) {
            throw new MomentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private Long extractIdFromToken(String token) {
        Jws<Claims> verifiedJwt = Jwts.parser()
                .verifyWith(new SecretKeySpec(accessSecretKey.getBytes(), "HmacSHA256"))
                .build()
                .parseSignedClaims(token);

        return Long.valueOf(verifiedJwt.getPayload().getSubject());
    }
}
