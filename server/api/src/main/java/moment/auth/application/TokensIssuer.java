package moment.auth.application;

import lombok.RequiredArgsConstructor;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
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
    public Tokens issueTokens(User user) {
        String accessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue)
        );
        refreshTokenRepository.save(refreshToken);

        return new Tokens(accessToken, refreshToken);
    }

    @Transactional
    public Tokens renewTokens(RefreshToken refreshToken) {
        User user = refreshToken.getUser();

        String newAccessToken = tokenManager.createAccessToken(user.getId(), user.getEmail());
        String newRefreshTokenValue = tokenManager.createRefreshToken(user.getId(), user.getEmail());

        refreshToken.renew(
                newRefreshTokenValue,
                tokenManager.getIssuedAtFromToken(newRefreshTokenValue),
                tokenManager.getExpirationTimeFromToken(newRefreshTokenValue));

        return new Tokens(newAccessToken, refreshToken);
    }
}
