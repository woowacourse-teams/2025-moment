package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.auth.dto.google.GoogleUserInfo;
import moment.user.domain.PendingUser;
import moment.user.infrastructure.PendingUserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PendingUserCacheService {

    private final PendingUserCache pendingUserCache;

    public PendingUser register(GoogleUserInfo googleUserInfo) {
        String email = googleUserInfo.getEmail();
        PendingUser pendingUser = new PendingUser(email, googleUserInfo);
        return pendingUserCache.save(pendingUser);
    }

    public PendingUser getPendingUser(String email) {
        return pendingUserCache.getPendingUser(email);
    }

    public void removePendingUser(String email) {
        pendingUserCache.remove(email);
    }
}
