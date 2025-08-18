package moment.auth.application;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TokensIssuer {

    private final TokenManager tokenManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Map<String, String> issueTokens(User user) {
        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue)
        );
        refreshTokenRepository.save(refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshTokenValue);

        return tokens;
    }

    @Transactional
    public Map<String, String> renewTokens(RefreshToken refreshToken) {
        Map<String, String> tokens = new HashMap<>();
        User user = refreshToken.getUser();

        String newAccessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String newRefreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        refreshToken.renew(
                newRefreshTokenValue,
                tokenManager.getIssuedAtFromToken(newRefreshTokenValue),
                tokenManager.getExpirationTimeFromToken(newRefreshTokenValue));

        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshTokenValue);

        return tokens;
    }
}
