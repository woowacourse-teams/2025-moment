package moment.auth.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.auth.dto.apple.ApplePublicKey;
import moment.auth.dto.apple.ApplePublicKeys;
import moment.auth.dto.apple.AppleUserInfo;
import moment.auth.service.auth.AppleOAuthClient;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AppleAuthClient implements AppleOAuthClient {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String ALLOWED_ALGORITHM = "RS256";
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${auth.apple.client-ids}")
    private List<String> allowedClientIds;

    /**
     * Identity Token 검증 및 사용자 정보 추출
     */
    public AppleUserInfo verifyAndGetUserInfo(String identityToken) {
        // 1. JWT 헤더에서 kid, alg 추출
        Map<String, String> header = parseHeader(identityToken);
        String kid = header.get("kid");
        String alg = header.get("alg");

        // 2. 알고리즘 검증 (RS256만 허용)
        validateAlgorithm(alg);

        // 3. 공개키 조회
        PublicKey publicKey = getPublicKey(kid, alg);

        // 4. JWT 검증 및 Claims 추출
        Claims claims = verifyAndGetClaims(identityToken, publicKey);

        // 5. AppleUserInfo 반환 (email 클레임은 사용자가 이메일 공유를 선택한 경우에만 존재)
        String email = claims.get("email", String.class);
        return new AppleUserInfo(claims.getSubject(), email);
    }

    /**
     * JWT 헤더 파싱 (서명 검증 없이)
     */
    private Map<String, String> parseHeader(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> headerMap = objectMapper.readValue(headerJson, new TypeReference<>() {});

            return Map.of(
                    "kid", (String) headerMap.get("kid"),
                    "alg", (String) headerMap.get("alg")
            );
        } catch (MomentException e) {
            throw e;
        } catch (Exception e) {
            throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
        }
    }

    /**
     * 알고리즘 검증 - RS256만 허용
     */
    private void validateAlgorithm(String alg) {
        if (!ALLOWED_ALGORITHM.equals(alg)) {
            throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
        }
    }

    /**
     * Apple 공개키 조회 (캐시에서 먼저 확인)
     */
    private PublicKey getPublicKey(String kid, String alg) {
        ApplePublicKeys publicKeys = getApplePublicKeys();

        return publicKeys.findMatchingKey(kid, alg)
                .map(this::generatePublicKey)
                .orElseGet(() -> {
                    // 캐시 무효화 후 재조회
                    evictApplePublicKeysCache();
                    ApplePublicKeys refreshedKeys = getApplePublicKeys();
                    return refreshedKeys.findMatchingKey(kid, alg)
                            .map(this::generatePublicKey)
                            .orElseThrow(() -> new MomentException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));
                });
    }

    /**
     * Apple JWKS 조회 (5분 캐싱)
     */
    @Cacheable(value = "applePublicKeys", unless = "#result == null")
    public ApplePublicKeys getApplePublicKeys() {
        try {
            return restTemplate.getForObject(APPLE_JWKS_URL, ApplePublicKeys.class);
        } catch (RestClientException e) {
            throw new MomentException(ErrorCode.APPLE_AUTH_SERVER_ERROR);
        }
    }

    /**
     * 캐시 무효화
     */
    @CacheEvict(value = "applePublicKeys", allEntries = true)
    public void evictApplePublicKeysCache() {
        // 캐시 무효화만 수행
    }

    /**
     * RSA PublicKey 생성
     */
    private PublicKey generatePublicKey(ApplePublicKey applePublicKey) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.n());
            byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.e());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new MomentException(ErrorCode.APPLE_PUBLIC_KEY_GENERATION_FAILED);
        }
    }

    /**
     * JWT 서명 검증 및 Claims 추출
     */
    private Claims verifyAndGetClaims(String token, PublicKey publicKey) {
        try {
            Claims claims = Jwts.parser()
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // iss 검증
            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
            }

            // aud 검증
            String audience = claims.getAudience().iterator().next();
            if (!allowedClientIds.contains(audience)) {
                throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new MomentException(ErrorCode.APPLE_TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException e) {
            throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
        } catch (MomentException e) {
            throw e;
        } catch (Exception e) {
            throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
        }
    }
}
