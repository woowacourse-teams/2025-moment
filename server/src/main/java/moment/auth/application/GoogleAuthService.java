package moment.auth.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.user.domain.NicknameGenerator;
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
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthClient googleAuthClient;
    private final NicknameGenerator nicknameGenerator;

    @Transactional
    public String loginOrSignUp(String authorizationCode) {
        GoogleAccessToken accessToken = googleAuthClient.getAccessToken(authorizationCode);

        GoogleUserInfo googleUserInfo = googleAuthClient.getUserInfo(accessToken.getAccessToken());

        String email = googleUserInfo.getEmail();

        Optional<User> findUser = userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE);

        if (findUser.isPresent()) {
            User user = findUser.get();
            return tokenManager.createToken(user.getId(), user.getEmail());
        }

        User savedUser = addUser(email, googleUserInfo.getSub());
        return tokenManager.createToken(savedUser.getId(), savedUser.getEmail());
    }

    private User addUser(String email, String sub) {
        String encodedPassword = passwordEncoder.encode(sub);
        User user = new User(email, encodedPassword, nicknameGenerator.generateNickname(), ProviderType.GOOGLE);

        return userRepository.save(user);
    }
}
