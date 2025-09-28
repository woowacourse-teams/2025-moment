package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.application.NicknameGenerateService;
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
import org.springframework.mock.web.MockHttpServletRequest;
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

    @Mock
    private GoogleAuthClient googleAuthClient;

    @Mock
    private NicknameGenerateService nicknameGenerateService;

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

        LocalDate todayLocalDate = LocalDate.now();
        LocalDate futureLocalDate = todayLocalDate.plusDays(30);

        Date todayDate = Date.from(todayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date futureDate = Date.from(futureLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Tokens tokens = new Tokens(accessToken,
                new RefreshToken(refreshToken, user, todayDate, futureDate));

        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(tokensIssuer.issueTokens(any()))
                .willReturn(tokens);

        // when
        Tokens result = authService.login(request);

        // then
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken().getTokenValue()).isEqualTo(refreshToken);
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
        String refreshTokenValue = "existingRefreshToken";
        MockHttpServletRequest request = new MockHttpServletRequest();

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshTokenValue);
        request.setCookies(refreshTokenCookie);
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 60000) // 만료되지 않은 토큰
        );

        given(refreshTokenRepository.findByTokenValue(refreshTokenValue)).willReturn(Optional.of(refreshToken));

        String newAccessToken = "newAccessToken";
        String newRefreshTokenValue = "newRefreshToken";
        RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenValue,
                user,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 60000));

        Tokens tokens = new Tokens(newAccessToken, newRefreshToken);

        given(tokensIssuer.renewTokens(refreshToken)).willReturn(tokens);

        // when
        Tokens result = authService.refresh(request);

        // then
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken().getTokenValue()).isEqualTo(newRefreshTokenValue);
        then(refreshTokenRepository).should(times(1)).findByTokenValue("existingRefreshToken");
        then(tokensIssuer).should(times(1)).renewTokens(refreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 요청하면 예외가 발생한다.")
    void refresh_fail_tokenNotFound() {
        // given
        String refreshTokenValue = "nonExistentToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshTokenValue);

        request.setCookies(refreshTokenCookie);
        given(refreshTokenRepository.findByTokenValue(refreshTokenValue)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 요청하면 예외가 발생한다.")
    void refresh_fail_tokenExpired() {
        // given
        String refreshTokenValue = "expiredRefreshToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshTokenValue);

        request.setCookies(refreshTokenCookie);

        RefreshToken expiredToken = new RefreshToken(
                "expiredRefreshToken",
                user,
                new Date(),
                new Date()
        );

        given(refreshTokenRepository.findByTokenValue(refreshTokenValue)).willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.TOKEN_EXPIRED.getMessage());
    }

    @Test
    void 기존에_가입된_유저가_구글_로그인을_하면_토큰을_반환한다() {
        // given
        String email = "mimi@icloud.com";
        String expectedAccessToken = "testAccessToken";
        String expectedRefreshToken = "testRefreshToken";

        GoogleAccessToken googleAccessToken = new GoogleAccessToken("accessToken", 1800, "scope", "tokenType",
                "idToken");
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        User existingUser = new User(email, "password", "mimi", ProviderType.GOOGLE);

        Tokens tokens = new Tokens(
                expectedAccessToken,
                new RefreshToken(expectedRefreshToken, existingUser, new Date(), new Date()));

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE)).willReturn(
                Optional.of(existingUser));
        given(tokensIssuer.issueTokens(existingUser)).willReturn(tokens);

        // when
        authService.googleLogin("authorizationCode");

        // then
        then(userRepository).should(times(0)).save(any(User.class));
    }

    @Test
    void 유저가_구글_로그인을_하면_토큰을_반환한다() {
        // given
        String expectedAccessToken = "testAccessToken";
        String expectedRefreshToken = "testRefreshToken";
        String email = "mimi@icloud.com";
        GoogleAccessToken googleAccessToken = new GoogleAccessToken("accessToken", 1800, "scope", "tokenType",
                "idToken");
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);

        User user = new User(email, "encodedPassword", "mimi", ProviderType.GOOGLE);

        Tokens tokens = new Tokens(
                expectedAccessToken,
                new RefreshToken(expectedRefreshToken, user, new Date(), new Date()));

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE)).willReturn(Optional.of(user));
        given(tokensIssuer.issueTokens(user)).willReturn(tokens);

        // when
        Tokens actualTokens = authService.googleLogin("authorizationCode");

        // then
        assertAll(
                () -> assertThat(actualTokens.getAccessToken()).isEqualTo(expectedAccessToken),
                () -> assertThat(actualTokens.getRefreshToken().getTokenValue()).isEqualTo(expectedRefreshToken)
        );
    }
}
