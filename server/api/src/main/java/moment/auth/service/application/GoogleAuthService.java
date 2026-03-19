package moment.auth.service.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.Tokens;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.service.auth.GoogleOAuthClient;
import moment.auth.service.auth.TokensIssuer;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.application.NicknameGenerateApplicationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuthClient googleAuthClient;
    private final NicknameGenerateApplicationService nicknameGenerateApplicationService;
    private final TokensIssuer tokensIssuer;

    @Transactional
    public Tokens loginOrSignUp(String authorizationCode) {
        GoogleAccessToken googleAccessToken = googleAuthClient.getAccessToken(authorizationCode);

        GoogleUserInfo googleUserInfo = googleAuthClient.getUserInfo(googleAccessToken.getAccessToken());

        String email = googleUserInfo.getEmail();

        Optional<User> findUser = userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE);

        if (findUser.isPresent()) {
            User user = findUser.get();

            return tokensIssuer.issueTokens(user);
        }

        User savedUser = addUser(email, googleUserInfo.getSub());

        return tokensIssuer.issueTokens(savedUser);
    }

    private User addUser(String email, String sub) {
        String encodedPassword = passwordEncoder.encode(sub);
        User user = new User(email, encodedPassword, nicknameGenerateApplicationService.generate(),
                ProviderType.GOOGLE);

        return userRepository.save(user);
    }
}
