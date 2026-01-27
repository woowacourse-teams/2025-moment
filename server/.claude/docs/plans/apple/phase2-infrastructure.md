# Phase 2: Infrastructure 구현 (AppleAuthClient)

## 목표
Apple JWKS 조회, JWT 검증을 담당하는 AppleAuthClient를 구현합니다.

---

## 1. 의존성 확인

### 필요 라이브러리
- `io.jsonwebtoken:jjwt-api` (JWT 파싱)
- `io.jsonwebtoken:jjwt-impl` (JWT 구현)
- `io.jsonwebtoken:jjwt-jackson` (JWT JSON 처리)
- `org.springframework.boot:spring-boot-starter-cache` (캐싱)

### build.gradle 확인
기존 JWT 관련 의존성이 있는지 확인 필요

---

## 2. AppleAuthClient 구현

### 파일 위치
`src/main/java/moment/auth/infrastructure/AppleAuthClient.java`

### 책임
1. Apple JWKS 조회 (캐싱 적용)
2. JWT 헤더에서 kid, alg 추출
3. 공개키로 JWT 서명 검증
4. Claims 검증 (iss, aud, exp)
5. AppleUserInfo 반환

### 구현
```java
package moment.auth.infrastructure;

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
public class AppleAuthClient {

    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String ALLOWED_ALGORITHM = "RS256";
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final RestTemplate restTemplate;

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

        // 5. AppleUserInfo 반환
        return new AppleUserInfo(claims.getSubject());
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
            // 간단한 JSON 파싱 (Jackson ObjectMapper 사용 권장)
            // 여기서는 jjwt의 내부 파서 활용
            var unsignedJwt = Jwts.parser()
                .unsecured()
                .build()
                .parseUnsecuredClaims(parts[0] + "." + parts[1] + ".");

            var header = unsignedJwt.getHeader();
            return Map.of(
                "kid", (String) header.get("kid"),
                "alg", (String) header.get("alg")
            );
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
        }
    }
}
```

---

## 3. 테스트 전략

### 3.1 단위 테스트 파일 위치
`src/test/java/moment/auth/infrastructure/AppleAuthClientTest.java`

### 3.2 테스트 케이스

```java
package moment.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import moment.auth.dto.apple.ApplePublicKey;
import moment.auth.dto.apple.ApplePublicKeys;
import moment.auth.dto.apple.AppleUserInfo;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AppleAuthClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AppleAuthClient appleAuthClient;

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        ReflectionTestUtils.setField(appleAuthClient, "allowedClientIds", List.of("com.moment.app"));
    }

    @Nested
    @DisplayName("verifyAndGetUserInfo")
    class VerifyAndGetUserInfoTest {

        @Test
        @DisplayName("유효한 토큰으로 사용자 정보를 반환한다")
        void success() {
            // given
            String validToken = createValidToken("test-sub", "com.moment.app");
            ApplePublicKeys mockKeys = createMockApplePublicKeys();
            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(mockKeys);

            // when
            AppleUserInfo result = appleAuthClient.verifyAndGetUserInfo(validToken);

            // then
            assertThat(result.sub()).isEqualTo("test-sub");
        }

        @Test
        @DisplayName("RS256이 아닌 알고리즘이면 예외를 던진다")
        void invalidAlgorithm() {
            // given
            String tokenWithWrongAlg = createTokenWithAlgorithm("HS256");

            // when & then
            assertThatThrownBy(() -> appleAuthClient.verifyAndGetUserInfo(tokenWithWrongAlg))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLE_TOKEN_INVALID);
        }

        @Test
        @DisplayName("만료된 토큰이면 AP-002 예외를 던진다")
        void expiredToken() {
            // given
            String expiredToken = createExpiredToken();
            ApplePublicKeys mockKeys = createMockApplePublicKeys();
            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(mockKeys);

            // when & then
            assertThatThrownBy(() -> appleAuthClient.verifyAndGetUserInfo(expiredToken))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLE_TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("허용되지 않은 aud면 예외를 던진다")
        void invalidAudience() {
            // given
            String tokenWithWrongAud = createValidToken("test-sub", "com.wrong.app");
            ApplePublicKeys mockKeys = createMockApplePublicKeys();
            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(mockKeys);

            // when & then
            assertThatThrownBy(() -> appleAuthClient.verifyAndGetUserInfo(tokenWithWrongAud))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLE_TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("getApplePublicKeys")
    class GetApplePublicKeysTest {

        @Test
        @DisplayName("Apple JWKS 조회 성공")
        void success() {
            // given
            ApplePublicKeys mockKeys = createMockApplePublicKeys();
            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(mockKeys);

            // when
            ApplePublicKeys result = appleAuthClient.getApplePublicKeys();

            // then
            assertThat(result.keys()).hasSize(1);
        }

        @Test
        @DisplayName("Apple 서버 오류 시 AP-005 예외")
        void serverError() {
            // given
            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenThrow(new RestClientException("Connection failed"));

            // when & then
            assertThatThrownBy(() -> appleAuthClient.getApplePublicKeys())
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLE_AUTH_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("공개키 조회")
    class PublicKeyTest {

        @Test
        @DisplayName("kid가 일치하는 키가 없으면 캐시 무효화 후 재조회")
        void cacheEvictAndRetry() {
            // given
            String validToken = createValidToken("test-sub", "com.moment.app");
            ApplePublicKeys emptyKeys = new ApplePublicKeys(List.of());
            ApplePublicKeys validKeys = createMockApplePublicKeys();

            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(emptyKeys)
                .thenReturn(validKeys);

            // when
            AppleUserInfo result = appleAuthClient.verifyAndGetUserInfo(validToken);

            // then
            assertThat(result.sub()).isEqualTo("test-sub");
            verify(restTemplate, times(2)).getForObject(anyString(), eq(ApplePublicKeys.class));
        }

        @Test
        @DisplayName("재조회 후에도 키가 없으면 AP-003 예외")
        void keyNotFoundAfterRetry() {
            // given
            String validToken = createValidToken("test-sub", "com.moment.app");
            ApplePublicKeys emptyKeys = new ApplePublicKeys(List.of());

            when(restTemplate.getForObject(anyString(), eq(ApplePublicKeys.class)))
                .thenReturn(emptyKeys);

            // when & then
            assertThatThrownBy(() -> appleAuthClient.verifyAndGetUserInfo(validToken))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
        }
    }

    // Helper methods
    private String createValidToken(String sub, String aud) {
        return Jwts.builder()
            .header().keyId("test-kid").and()
            .subject(sub)
            .issuer("https://appleid.apple.com")
            .audience().add(aud).and()
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
            .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
            .header().keyId("test-kid").and()
            .subject("test-sub")
            .issuer("https://appleid.apple.com")
            .audience().add("com.moment.app").and()
            .issuedAt(new Date(System.currentTimeMillis() - 7200000))
            .expiration(new Date(System.currentTimeMillis() - 3600000))
            .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
            .compact();
    }

    private String createTokenWithAlgorithm(String alg) {
        // HS256 등 다른 알고리즘으로 서명된 토큰 (테스트용)
        // 실제로는 헤더만 다르게 만들어서 alg 검증 테스트
        return "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3Qta2lkIn0.eyJzdWIiOiJ0ZXN0In0.signature";
    }

    private ApplePublicKeys createMockApplePublicKeys() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        String n = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(rsaPublicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

        ApplePublicKey key = new ApplePublicKey("RSA", "test-kid", "sig", "RS256", n, e);
        return new ApplePublicKeys(List.of(key));
    }
}
```

---

## 4. RestTemplate Bean 설정

### 파일 위치
`src/main/java/moment/global/config/RestTemplateConfig.java` (없으면 생성)

### 구현
```java
package moment.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

## 5. 캐시 설정

### 파일 위치
기존 캐시 설정 파일 또는 새로 생성

### CacheConfig.java (없으면 생성)
```java
package moment.global.config;

import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("applePublicKeys");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10));
        return cacheManager;
    }
}
```

### build.gradle 의존성 (필요시)
```gradle
implementation 'com.github.ben-manes.caffeine:caffeine'
```

---

## 구현 순서 (TDD)

### Step 1: RestTemplate Bean 확인/설정
1. 기존 RestTemplate Bean 확인
2. 없으면 `RestTemplateConfig.java` 생성

### Step 2: 캐시 설정
1. 캐시 의존성 확인 (Caffeine 또는 기본 캐시)
2. `CacheConfig.java` 생성 또는 수정
3. `@EnableCaching` 활성화 확인

### Step 3: AppleAuthClient 테스트 작성
1. `AppleAuthClientTest.java` 작성
2. 테스트 실행 → 실패 확인 (Red)

### Step 4: AppleAuthClient 구현
1. `AppleAuthClient.java` 구현
2. 테스트 통과 확인 (Green)

### Step 5: 리팩토링 (필요시)
1. 코드 정리
2. 테스트 재실행

---

## 체크리스트

- [ ] RestTemplate Bean 설정 확인/생성
- [ ] 캐시 설정 확인/생성 (Caffeine)
- [ ] `AppleAuthClientTest` 작성
- [ ] `AppleAuthClient` 구현
- [ ] 캐싱 동작 테스트
- [ ] 알고리즘 검증 (RS256만 허용) 테스트
- [ ] aud 검증 (허용 리스트) 테스트
- [ ] 만료 토큰 검증 테스트
- [ ] 공개키 미일치 시 재조회 테스트
- [ ] `./gradlew fastTest` 전체 통과