package moment.auth.service.application;

import moment.auth.dto.google.GoogleAccessToken;
import moment.auth.dto.google.GoogleUserInfo;

public interface GoogleOAuthClient {

    GoogleAccessToken getAccessToken(String authorizationCode);

    GoogleUserInfo getUserInfo(String accessToken);
}
