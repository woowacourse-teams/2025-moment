package moment.auth.application;

import moment.user.dto.request.Authentication;

public interface TokenManager {

    String createToken(Long id, String email);

    Authentication extractAuthentication(String token);
}
