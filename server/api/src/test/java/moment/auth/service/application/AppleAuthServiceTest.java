package moment.auth.service.application;

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
import moment.auth.service.auth.TokensIssuer;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.application.NicknameGenerateApplicationService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AppleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppleOAuthClient appleAuthClient;

    @Mock
    private NicknameGenerateApplicationService nicknameGenerateApplicationService;

    @Mock
    private TokensIssuer tokensIssuer;

    @InjectMocks
    private AppleAuthService appleAuthService;

    @Nested
    class loginOrSignUp {

        @Test
        void Apple_이메일이_있는_기존_사용자면_토큰을_발급한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = "user@icloud.com";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, appleEmail);
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
        void Apple_이메일이_없는_기존_사용자면_hash_이메일로_조회하여_토큰을_발급한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, null);
            String displayEmail = userInfo.resolveDisplayEmail();

            User existingUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(displayEmail, ProviderType.APPLE))
                    .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void 기존_sub_apple_user_사용자면_fallback_조회_후_이메일을_업데이트한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, null);
            String displayEmail = userInfo.resolveDisplayEmail();
            String legacyEmail = userInfo.toLegacyEmail();

            User legacyUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            // 1차 조회 실패 (새 형식 이메일로는 못 찾음)
            when(userRepository.findByEmailAndProviderType(displayEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            // fallback 조회 성공 (기존 sub@apple.user 형식으로 찾음)
            when(userRepository.findByEmailAndProviderType(legacyEmail, ProviderType.APPLE))
                    .thenReturn(Optional.of(legacyUser));
            when(tokensIssuer.issueTokens(legacyUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(legacyUser).updateEmail(displayEmail);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void 신규_사용자에_Apple_이메일이_있으면_해당_이메일로_생성한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = "user@icloud.com";
            String legacyEmail = appleUserId + "@apple.user";
            String nickname = "행복한별123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, appleEmail);
            User savedUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndProviderType(legacyEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn("encoded-password");
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(tokensIssuer.issueTokens(savedUser)).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(appleEmail);
            assertThat(capturedUser.getProviderType()).isEqualTo(ProviderType.APPLE);
        }

        @Test
        void 신규_사용자에_Apple_이메일이_없으면_짧은_이메일로_생성한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, null);
            String displayEmail = userInfo.resolveDisplayEmail();
            String legacyEmail = userInfo.toLegacyEmail();
            String nickname = "행복한별123";

            User savedUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(displayEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmailAndProviderType(legacyEmail, ProviderType.APPLE))
                    .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn("encoded-password");
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(tokensIssuer.issueTokens(savedUser)).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(displayEmail);
            assertThat(capturedUser.getEmail()).startsWith("apple_");
            assertThat(capturedUser.getEmail()).endsWith("@apple.app");
        }

        @Test
        void 동일_sub로_재로그인하면_같은_사용자로_인식한다() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId, null);
            String displayEmail = userInfo.resolveDisplayEmail();

            User existingUser = mock(User.class);
            RefreshToken refreshToken = mock(RefreshToken.class);
            Tokens expectedTokens = new Tokens("access-token", refreshToken);

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(displayEmail, ProviderType.APPLE))
                    .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            verify(userRepository).findByEmailAndProviderType(displayEmail, ProviderType.APPLE);
            verify(tokensIssuer).issueTokens(existingUser);
        }

        @Test
        void AppleAuthClient에서_예외_발생_시_그대로_전파된다() {
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
