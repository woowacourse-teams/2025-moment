package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.apple.AppleUserInfo;
import moment.auth.infrastructure.AppleAuthClient;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.application.NicknameGenerateApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AppleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppleAuthClient appleAuthClient;

    @Mock
    private NicknameGenerateApplicationService nicknameGenerateApplicationService;

    @Mock
    private TokensIssuer tokensIssuer;

    @InjectMocks
    private AppleAuthService appleAuthService;

    @Nested
    @DisplayName("loginOrSignUp")
    class LoginOrSignUpTest {

        @Test
        @DisplayName("기존 사용자면 토큰만 발급한다")
        void existingUser_returnsTokens() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User existingUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                    .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("신규 사용자면 회원가입 후 토큰을 발급한다")
        void newUser_createsUserAndReturnsTokens() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";
            String nickname = "행복한별123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User savedUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn("encoded-password");
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(tokensIssuer.issueTokens(savedUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("신규 사용자 생성 시 올바른 정보로 User를 생성한다")
        void newUser_createsUserWithCorrectInfo() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";
            String nickname = "행복한별123";
            String encodedPassword = "encoded-password";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn(encodedPassword);
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(tokensIssuer.issueTokens(any(User.class))).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(appleEmail);
            assertThat(capturedUser.getNickname()).isEqualTo(nickname);
            assertThat(capturedUser.getProviderType()).isEqualTo(ProviderType.APPLE);
        }

        @Test
        @DisplayName("동일 sub로 재로그인하면 같은 사용자로 인식한다")
        void sameSubReturnsExistingUser() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User existingUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                    .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            verify(userRepository).findByEmailAndProviderType(appleEmail, ProviderType.APPLE);
            verify(tokensIssuer).issueTokens(existingUser);
        }

        @Test
        @DisplayName("AppleAuthClient에서 예외 발생 시 그대로 전파된다")
        void propagatesExceptionFromAppleAuthClient() {
            // given
            String identityToken = "invalid.token";
            when(appleAuthClient.verifyAndGetUserInfo(identityToken))
                    .thenThrow(new RuntimeException("Token validation failed"));

            // when & then
            assertThatThrownBy(() -> appleAuthService.loginOrSignUp(identityToken))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
