package moment.auth.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.GoogleAuthClient;
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
public class

AuthService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final EmailService emailService;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokensIssuer tokensIssuer;
    private final GoogleAuthClient googleAuthClient;

    @Transactional
    public Tokens login(LoginRequest request) {
        User user = userRepository.findByEmailAndProviderType(request.email(), ProviderType.EMAIL)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new MomentException(ErrorCode.USER_LOGIN_FAILED);
        }

        return tokensIssuer.issueTokens(user);
    }

    @Transactional
    public Tokens googleLogin(String authorizationCode) {
        GoogleUserInfo googleUserInfo = getGoogleUserInfo(authorizationCode);
        String email = googleUserInfo.getEmail();

        Optional<User> findUser = userRepository.findByEmailAndProviderType(email, ProviderType.GOOGLE);
        if (findUser.isPresent()) {
            User user = findUser.get();
            return tokensIssuer.issueTokens(user);
        }

        throw new MomentException(ErrorCode.USER_NOT_FOUND);
    }

    public GoogleUserInfo getGoogleUserInfo(String authorizationCode) {
        GoogleAccessToken googleAccessToken = googleAuthClient.getAccessToken(authorizationCode);
        return googleAuthClient.getUserInfo(googleAccessToken.getAccessToken());
    }

    public String getPendingToken(GoogleUserInfo googleUserInfo) {
        return tokenManager.createPendingToken(googleUserInfo.getEmail());
    }

    public String getPendingAuthenticationByToken(String token) {
        return tokenManager.extractPendingAuthentication(token);
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
    public Tokens refresh(HttpServletRequest request) {
        String refreshTokenValue = extractRefreshTokenValue(request);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new MomentException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired(LocalDateTime.now())) {
            throw new MomentException(ErrorCode.TOKEN_EXPIRED);
        }

        return tokensIssuer.renewTokens(refreshToken);
    }

    private String extractRefreshTokenValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new MomentException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName()
                        .equals("refreshToken"))
                .findFirst()
                .orElseThrow(() -> new MomentException(ErrorCode.REFRESH_TOKEN_NOT_FOUND))
                .getValue();
    }

    @Transactional
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
