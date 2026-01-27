package moment.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
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
            String tokenWithWrongAlg = createTokenWithWrongAlgorithm();

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

    private String createValidToken(String sub, String aud) {
        return Jwts.builder()
                .header().keyId("test-kid").and()
                .subject(sub)
                .issuer("https://appleid.apple.com")
                .audience().add(aud).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
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
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    private String createTokenWithWrongAlgorithm() {
        return "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3Qta2lkIn0.eyJzdWIiOiJ0ZXN0In0.signature";
    }

    private ApplePublicKeys createMockApplePublicKeys() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        byte[] nBytes = rsaPublicKey.getModulus().toByteArray();
        byte[] eBytes = rsaPublicKey.getPublicExponent().toByteArray();

        // BigInteger에서 앞에 0x00이 추가될 수 있으므로 처리
        if (nBytes[0] == 0) {
            byte[] tmp = new byte[nBytes.length - 1];
            System.arraycopy(nBytes, 1, tmp, 0, tmp.length);
            nBytes = tmp;
        }

        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(nBytes);
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(eBytes);

        ApplePublicKey key = new ApplePublicKey("RSA", "test-kid", "sig", "RS256", n, e);
        return new ApplePublicKeys(List.of(key));
    }
}
