package moment.auth.application;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public Map<String, String> login(LoginRequest request) {
        User user = userRepository.findByEmailAndProviderType(request.email(), ProviderType.EMAIL)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new MomentException(ErrorCode.USER_LOGIN_FAILED);
        }

        Map<String, String> tokens = new HashMap<>();

        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshTokenWithoutId = new RefreshToken(
                refreshToken,
                user,
                tokenManager.getIssuedAtFromToken(refreshToken),
                tokenManager.getExpirationTimeFromToken(refreshToken));

        refreshTokenRepository.save(refreshTokenWithoutId);

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    public Authentication getAuthenticationByToken(String token) {
        return tokenManager.extractAuthentication(token);
    }

    public LoginCheckResponse loginCheck(String token) {
        if (token == null || token.isEmpty()) {
            return LoginCheckResponse.createNotLogged();
        }
        return LoginCheckResponse.createLogged();
    }

    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));

        if (refreshTokenRepository.ExistByUser(user)) {
            refreshTokenRepository.deleteByUser(user);
        }
    }
}
