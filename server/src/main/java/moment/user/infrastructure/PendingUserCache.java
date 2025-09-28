package moment.user.infrastructure;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.PendingUser;
import org.springframework.stereotype.Component;

@Component
public class PendingUserCache {

    private static final int EXPIRATION_TIME = 5;
    private static final TimeUnit EXPIRATION_TIME_UNIT = TimeUnit.MINUTES;

    private final Cache<String, PendingUser> cache;

    public PendingUserCache() {
        this(EXPIRATION_TIME, EXPIRATION_TIME_UNIT);
    }

    public PendingUserCache(long expirationTime, TimeUnit timeUnit) {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(expirationTime, timeUnit)
            .build();
    }

    public PendingUser save(PendingUser user) {
        cache.put(user.email(), user);
        return getPendingUser(user.email());
    }

    public PendingUser getPendingUser(String email) {
        PendingUser user = cache.getIfPresent(email);
        if (user == null) {
            throw new MomentException(ErrorCode.PENDING_USER_NOT_FOUND);
        }
        return user;
    }

    public void remove(String email) {
        cache.invalidate(email);
    }
}
