package moment.auth.application;

import io.jsonwebtoken.Claims;

public interface TokenManager {

    String createToken(Long id, String email);

    Claims extractClaims(String token);
}
