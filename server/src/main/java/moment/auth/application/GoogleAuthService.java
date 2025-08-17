package moment.auth.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.infrastructure.GoogleAuthClient;
import moment.auth.infrastructure.RefreshTokenRepository;
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
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthClient googleAuthClient;
    private final NicknameGenerateService nicknameGenerateService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Map<String, String> loginOrSignUp(String authorizationCode) {
        GoogleAccessToken googleAccessToken = googleAuthClient.getAccessToken(authorizationCode);

        GoogleUserInfo googleUserInfo = googleAuthClient.getUserInfo(googleAccessToken.getAccessToken());

        String email = googleUserInfo.getEmail();

        Optional<User> findUser = userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE);

        Map<String, String> tokens = new HashMap<>();

        if (findUser.isPresent()) {
            User user = findUser.get();
            String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
            String refreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

            RefreshToken refreshTokenWithoutId = new RefreshToken(
                    refreshTokenValue,
                    user,
                    tokenManager.getIssuedAtFromToken(refreshTokenValue),
                    tokenManager.getExpirationTimeFromToken(refreshTokenValue));

            refreshTokenRepository.save(refreshTokenWithoutId);

            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshTokenValue);

            return tokens;
        }

        User savedUser = addUser(email, googleUserInfo.getSub());
        String accessToken = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshTokenValue = tokenManager.createRefreshToken(savedUser.getId(), savedUser.getEmail());

        RefreshToken refreshTokenWithoutId = new RefreshToken(
                refreshTokenValue,
                savedUser,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue));

        refreshTokenRepository.save(refreshTokenWithoutId);

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshTokenValue);
        return tokens;
    }

    private User addUser(String email, String sub) {
        String encodedPassword = passwordEncoder.encode(sub);
        User user = new User(email, encodedPassword, nicknameGenerateService.createRandomNickname(),
                ProviderType.GOOGLE);

        return userRepository.save(user);
    }
}
