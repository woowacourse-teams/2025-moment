package moment.auth.application;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.user.application.NicknameGenerateService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthClient googleAuthClient;
    private final NicknameGenerateService nicknameGenerateService;
    private final TokensIssuer tokensIssuer;

    @Transactional
    public Map<String, String> loginOrSignUp(String authorizationCode) {
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
        User user = new User(email, encodedPassword, nicknameGenerateService.createRandomNickname(),
                ProviderType.GOOGLE);

        return userRepository.save(user);
    }
}
