package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.RefreshTokenRequest;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthServiceTest {

    private final String email = "ekorea623@gmail.com";

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("ekorea623@gmail.com", "moment1234!", "drago", ProviderType.EMAIL);
    }

    @Test
    void 일반_로그인에_성공한다() {
        // given
        LoginRequest request = new LoginRequest(email, "1q2w3e4r");

        given(userRepository.findByEmailAndProviderType(email, ProviderType.EMAIL))
                .willReturn(Optional.of(user));

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(tokenManager.createAccessToken(any(), any())).willReturn(accessToken);
        given(tokenManager.createRefreshToken(any(), any())).willReturn(refreshToken);
        given(tokenManager.getIssuedAtFromToken(any())).willReturn(new Date());
        given(tokenManager.getExpirationTimeFromToken((any()))).willReturn(new Date());

        // when
        Map<String, String> tokens = authService.login(request);

        // then
        assertThat(tokens.get("accessToken")).isEqualTo(accessToken);
        assertThat(tokens.get("refreshToken")).isEqualTo(refreshToken);
    }

    @Test
    void 일반_로그인시_존재하지_않는_이메일을_입력한_경우_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest(email, "1q2w3e4r");
        given(userRepository.findByEmailAndProviderType(email, ProviderType.EMAIL))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_LOGIN_FAILED);
    }

    @Test
    void 일반_로그인시_비밀번호를_잘못_입력한_경우_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest(email, "1q2w3e4");
        given(userRepository.findByEmailAndProviderType(email, ProviderType.EMAIL))
                .willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_LOGIN_FAILED);
    }

    @Test
    void 토큰에서_인증정보를_추출할_수_있다() {
        // given
        String token = "validToken";
        Authentication authentication = new Authentication(1L);

        given(tokenManager.extractAuthentication(token)).willReturn(authentication);

        // when & then
        assertThat(authService.getAuthenticationByToken(token)).isEqualTo(authentication);
    }

    @Test
    void 로그아웃에_성공한다() {
        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(refreshTokenRepository.existsByUser(any())).willReturn(true);

        // when
        authService.logout(1L);

        // then
        then(refreshTokenRepository).should(times(1)).deleteByUser(any());
    }

    @Test
    void 로그아웃시_유저가_없다면_예외가_발생한다() {
        // given
        given(userRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.logout(1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 유효한_리프레시_토큰으로_새로운_엑세스_토큰과_리프레시_토큰을_발급받는다() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest("existingRefreshToken");
        RefreshToken refreshToken = new RefreshToken(
                "existingRefreshToken",
                user,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 1000)
        );

        given(refreshTokenRepository.findByTokenValue(request.refreshToken())).willReturn(Optional.of(refreshToken));
        given(tokenManager.createAccessToken(user.getId(), user.getEmail())).willReturn("newAccessToken");
        given(tokenManager.createRefreshToken(user.getId(), user.getEmail())).willReturn("newRefreshToken");
        given(tokenManager.getIssuedAtFromToken(anyString())).willReturn(new Date(System.currentTimeMillis()));
        given(tokenManager.getExpirationTimeFromToken(anyString())).willReturn(
                new Date(System.currentTimeMillis() + 10000));

        // when
        Map<String, String> tokens = authService.refresh(request);

        // then
        assertThat(tokens.get("accessToken")).isEqualTo("newAccessToken");
        assertThat(tokens.get("refreshToken")).isEqualTo("newRefreshToken");
        verify(refreshTokenRepository).findByTokenValue("existingRefreshToken");
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 요청하면 예외가 발생한다.")
    void refresh_fail_tokenNotFound() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest("nonExistentToken");
        given(refreshTokenRepository.findByTokenValue(request.refreshToken())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 요청하면 예외가 발생한다.")
    void refresh_fail_tokenExpired() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest("expiredRefreshToken");
        RefreshToken expiredToken = new RefreshToken(
                "expiredRefreshToken",
                user,
                new Date(),
                new Date()
        );
        given(refreshTokenRepository.findByTokenValue(request.refreshToken())).willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.TOKEN_EXPIRED.getMessage());
    }
}