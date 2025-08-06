package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.user.domain.NicknameGenerator;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleAuthClient googleAuthClient;

    @Mock
    private NicknameGenerator nicknameGenerator;

    @Test
    void 기존에_가입된_유저가_구글_로그인을_하면_토큰을_반환한다() {
        // given
        String email = "mimi@icloud.com";
        String expectedToken = "testToken";
        GoogleAccessToken googleAccessToken = new GoogleAccessToken("accessToken", 1800, "scope", "tokenType", "idToken");
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        User existingUser = new User(email, "password", "mimi", ProviderType.GOOGLE);

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE)).willReturn(Optional.of(existingUser));
        given(tokenManager.createToken(existingUser.getId(), existingUser.getEmail())).willReturn(expectedToken);

        // when
        String actualToken = googleAuthService.loginOrSignUp("authorizationCode");

        // then
        then(userRepository).should(times(0)).save(any(User.class));
        assertThat(actualToken).isEqualTo(expectedToken);
    }

    @Test
    void 신규_유저가_구글_로그인을_하면_회원가입_처리_후_토큰을_반환한다() {
        // given
        String expectedToken = "testToken";
        GoogleAccessToken googleAccessToken = new GoogleAccessToken("accessToken", 1800, "scope", "tokenType", "idToken");
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", "mimi@icloud.com", true);

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(any(String.class), any(ProviderType.class))).willReturn(Optional.empty());
        given(passwordEncoder.encode(any(String.class))).willReturn("encodedPassword");
        given(nicknameGenerator.generateNickname()).willReturn("mimi");
        given(userRepository.save(any(User.class))).willReturn(new User("mimi@icloud.com", "encodedPassword", "mimi", ProviderType.GOOGLE));
        given(tokenManager.createToken(any(), any(String.class))).willReturn(expectedToken);

        // when
        String actualToken = googleAuthService.loginOrSignUp("authorizationCode");

        // then
        then(userRepository).should(times(1)).save(any(User.class));
        assertThat(actualToken).isEqualTo(expectedToken);
    }
}
