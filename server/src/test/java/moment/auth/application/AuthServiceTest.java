package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.dto.request.RefreshTokenRequest;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.application.UserQueryService;
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
    private EmailService emailService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokensIssuer tokensIssuer;

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
        given(tokensIssuer.issueTokens(any()))
                .willReturn(Map.of("accessToken", accessToken, "refreshToken", refreshToken));

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
    void 비밀번호_재설정에_성공한다() {
        // given
        PasswordResetRequest request = new PasswordResetRequest(email, "valid-token", "newPassword123!",
                "newPassword123!");
        User user = new User(email, "encodedOldPassword", "drago", ProviderType.EMAIL);
        String encryptedNewPassword = "encryptedNewPassword123!";

        doNothing().when(emailService).verifyPasswordResetToken(request);
        given(userQueryService.findUserByEmailAndProviderType(request.email(), ProviderType.EMAIL))
                .willReturn(Optional.of(user));
        given(passwordEncoder.encode(request.newPassword())).willReturn(encryptedNewPassword);

        // when
        authService.resetPassword(request);

        // then
        then(emailService).should(times(1)).verifyPasswordResetToken(request);
        then(userQueryService).should(times(1)).findUserByEmailAndProviderType(request.email(), ProviderType.EMAIL);
        assertThat(user.getPassword()).isEqualTo(encryptedNewPassword);
    }

    @Test
    void 비밀번호_재설정시_새_비밀번호와_확인_비밀번호가_다르면_예외가_발생한다() {
        // given
        PasswordResetRequest request = new PasswordResetRequest(email, "valid-token", "newPassword123!",
                "differentPassword");

        // when & then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCHED);
    }

    @Test
    void 비밀번호_재설정시_사용자를_찾을_수_없으면_예외가_발생한다() {
        // given
        PasswordResetRequest request = new PasswordResetRequest(email, "valid-token", "newPassword123!",
                "newPassword123!");

        doNothing().when(emailService).verifyPasswordResetToken(request);
        given(userQueryService.findUserByEmailAndProviderType(request.email(), ProviderType.EMAIL))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
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
                new Date(System.currentTimeMillis() + 60000) // 만료되지 않은 토큰
        );

        given(refreshTokenRepository.findByTokenValue(request.refreshToken())).willReturn(Optional.of(refreshToken));

        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        given(tokensIssuer.renewTokens(refreshToken))
                .willReturn(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));

        // when
        Map<String, String> tokens = authService.refresh(request);

        // then
        assertThat(tokens.get("accessToken")).isEqualTo(newAccessToken);
        assertThat(tokens.get("refreshToken")).isEqualTo(newRefreshToken);
        then(refreshTokenRepository).should(times(1)).findByTokenValue("existingRefreshToken");
        then(tokensIssuer).should(times(1)).renewTokens(refreshToken);
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
