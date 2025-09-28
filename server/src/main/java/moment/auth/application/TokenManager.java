package moment.auth.application;

import java.util.Date;
import moment.user.dto.request.Authentication;

public interface TokenManager {

    String createAccessToken(Long id, String email);

    String createRefreshToken(Long id, String email);

    Date getExpirationTimeFromToken(String token);

    Date getIssuedAtFromToken(String token);

    Authentication extractAuthentication(String token);

    String createPendingToken(String email);

    String extractPendingAuthentication(String token);
}
