package moment.auth.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Map;
import java.util.Optional;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.user.application.NicknameGenerateService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
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
class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleAuthClient googleAuthClient;

    @Mock
    private NicknameGenerateService nicknameGenerateService;

    @Mock
    private TokensIssuer tokensIssuer;

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

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE)).willReturn(
                Optional.of(existingUser));
        given(tokensIssuer.issueTokens(existingUser))
                .willReturn(Map.of("accessToken", expectedAccessToken, "refreshToken", expectedRefreshToken));

        // when
        googleAuthService.loginOrSignUp("authorizationCode");

        // then
        then(userRepository).should(times(0)).save(any(User.class));
    }

    @Test
    void 신규_유저가_구글_로그인을_하면_회원가입_처리_후_토큰을_반환한다() {
        // given
        String expectedAccessToken = "testAccessToken";
        String expectedRefreshToken = "testRefreshToken";
        String email = "mimi@icloud.com";
        GoogleAccessToken googleAccessToken = new GoogleAccessToken("accessToken", 1800, "scope", "tokenType",
                "idToken");
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        User newUser = new User(email, "encodedPassword", "mimi", ProviderType.GOOGLE);

        given(googleAuthClient.getAccessToken(any(String.class))).willReturn(googleAccessToken);
        given(googleAuthClient.getUserInfo(googleAccessToken.getAccessToken())).willReturn(googleUserInfo);
        given(userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE)).willReturn(Optional.empty());
        given(passwordEncoder.encode(any(String.class))).willReturn("encodedPassword");
        given(nicknameGenerateService.createRandomNickname()).willReturn("반짝이는 우주의 퀘이사");
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(tokensIssuer.issueTokens(newUser))
                .willReturn(Map.of("accessToken", expectedAccessToken, "refreshToken", expectedRefreshToken));

        // when
        googleAuthService.loginOrSignUp("authorizationCode");

        // then
        then(userRepository).should(times(1)).save(any(User.class));
    }
}
