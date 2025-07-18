package moment.auth.application;

import io.jsonwebtoken.Claims;
import moment.user.dto.request.Authentication;

public interface TokenManager {

    String createToken(Long id, String email);

    Claims extractClaims(String token);

    Authentication extractAuthentication(String token);
}
