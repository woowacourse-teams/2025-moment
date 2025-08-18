package moment.auth.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import moment.auth.domain.RefreshToken;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class TokensIssuerTest {

    @InjectMocks
    private TokensIssuer tokensIssuer;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private User user;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        user = new User("test@moment.com", "password123!", "tester", ProviderType.EMAIL);
        Field idField = User.class.getDeclaredField("id"); // User 클래스의 id 필드 가져오기
        idField.setAccessible(true);                       // private 접근 허용
        idField.set(user, 1L);
    }

    @Test
    void 새로운_액세스_토큰과_리프레시_토큰을_발급하고_새_리프레시_토큰을_저장한다() {
        // given
        String accessToken = "new-access-token";
        String refreshTokenValue = "new-refresh-token";
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10000);

        given(tokenManager.createAccessToken(anyLong(), anyString())).willReturn(accessToken);
        given(tokenManager.createRefreshToken(anyLong(), anyString())).willReturn(refreshTokenValue);
        given(tokenManager.getIssuedAtFromToken(refreshTokenValue)).willReturn(now);
        given(tokenManager.getExpirationTimeFromToken(refreshTokenValue)).willReturn(expiry);

        // when
        Map<String, String> tokens = tokensIssuer.issueTokens(user);

        // then
        then(refreshTokenRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("renewTokens: .")
    void 기존_리프레시_토큰을_갱신하고_새로운_액세스_토큰과_리프레시_토큰을_발급한다() {
        // given
        String newAccessToken = "renewed-access-token";
        String newRefreshTokenValue = "renewed-refresh-token";
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10000);

        RefreshToken oldRefreshToken = new RefreshToken("old-refresh-token", user, new Date(), new Date());

        given(tokenManager.createAccessToken(user.getId(), user.getEmail())).willReturn(newAccessToken);
        given(tokenManager.createRefreshToken(user.getId(), user.getEmail())).willReturn(newRefreshTokenValue);
        given(tokenManager.getIssuedAtFromToken(newRefreshTokenValue)).willReturn(now);
        given(tokenManager.getExpirationTimeFromToken(newRefreshTokenValue)).willReturn(expiry);

        // when
        Map<String, String> tokens = tokensIssuer.renewTokens(oldRefreshToken);

        // then
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
