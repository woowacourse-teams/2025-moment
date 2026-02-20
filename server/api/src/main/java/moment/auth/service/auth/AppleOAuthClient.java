package moment.auth.service.auth;

import moment.auth.dto.apple.AppleUserInfo;

public interface AppleOAuthClient {

    AppleUserInfo verifyAndGetUserInfo(String identityToken);
}
