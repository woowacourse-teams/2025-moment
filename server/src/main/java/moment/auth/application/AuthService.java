package moment.auth.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.RefreshTokenRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.application.UserQueryService;
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
    private final UserQueryService userQueryService;
    private final EmailService emailService;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Map<String, String> login(LoginRequest request) {
        User user = userRepository.findByEmailAndProviderType(request.email(), ProviderType.EMAIL)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new MomentException(ErrorCode.USER_LOGIN_FAILED);
        }

        Map<String, String> tokens = new HashMap<>();

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

    public Authentication getAuthenticationByToken(String token) {
        return tokenManager.extractAuthentication(token);
    }

    public LoginCheckResponse loginCheck(String token) {
        if (token == null || token.isEmpty()) {
            return LoginCheckResponse.createNotLogged();
        }
        return LoginCheckResponse.createLogged();
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));

        if (refreshTokenRepository.existsByUser(user)) {
            refreshTokenRepository.deleteByUser(user);
        }
    }

    @Transactional
    public Map<String, String> refresh(RefreshTokenRequest request) {
        // 동일한 리프레시 토큰이 존재하는지 확인
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(request.refreshToken())
                .orElseThrow(() -> new MomentException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 리프레시 토큰이 만료됐는지 확인
        if (refreshToken.isExpired(LocalDateTime.now())) {
            throw new MomentException(ErrorCode.TOKEN_EXPIRED);
        }

        // 위 경우를 모두 통과했다면 엑세스 토큰과 리프레시 토큰 재발급
        Map<String, String> tokens = new HashMap<>();
        User user = refreshToken.getUser();

        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        // 기존 리프레시 토큰 갱신
        refreshToken.renew(
                refreshTokenValue,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue));

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshTokenValue);

        return tokens;
    }

    public void resetPassword(PasswordResetRequest request) {
        if (!request.newPassword().equals(request.newPasswordCheck())) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }

        emailService.verifyPasswordResetToken(request);

        User user = userQueryService.findUserByEmailAndProviderType(request.email(), ProviderType.EMAIL)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
        String encryptedPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encryptedPassword);
    }
}
