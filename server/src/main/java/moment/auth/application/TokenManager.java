package moment.auth.application;

import io.jsonwebtoken.Claims;
import moment.user.dto.request.LoginUser;

public interface TokenManager {

    String createToken(Long id, String email);

    Claims extractClaims(String token);

    LoginUser getLoginUserByToken(String token);
}
