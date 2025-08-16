package moment.auth.application;

import moment.user.dto.request.Authentication;

public interface TokenManager {

    String createAccessToken(Long id, String email);

    String createRefreshToken(Long id, String email);

    Authentication extractAuthentication(String token);
}
