package moment.auth.service.application;

import moment.auth.dto.apple.AppleUserInfo;

public interface AppleOAuthClient {

    AppleUserInfo verifyAndGetUserInfo(String identityToken);
}
