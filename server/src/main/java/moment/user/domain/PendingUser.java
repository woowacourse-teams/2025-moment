package moment.user.domain;

import moment.auth.dto.google.GoogleUserInfo;

public record PendingUser(
        String email,
        GoogleUserInfo googleUserInfo
) {
    public String getPassword() {
        return googleUserInfo.getSub();
    }
}
