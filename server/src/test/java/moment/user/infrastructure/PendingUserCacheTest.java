package moment.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import moment.auth.dto.google.GoogleUserInfo;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.PendingUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PendingUserCacheTest {

    private PendingUserCache pendingUserCache;

    @BeforeEach
    void setUp() {
        pendingUserCache = new PendingUserCache();
    }

    @Test
    void 임시_유저_정보를_캐시에_저장하고_조회한다() {
        // given
        String email = "test@moment.com";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        PendingUser pendingUser = new PendingUser(email, googleUserInfo);

        // when
        pendingUserCache.save(pendingUser);
        PendingUser foundUser = pendingUserCache.getPendingUser(email);

        // then
        assertAll(
            () -> assertThat(foundUser).isNotNull(),
            () -> assertThat(foundUser.email()).isEqualTo(email),
            () -> assertThat(foundUser.googleUserInfo()).isEqualTo(googleUserInfo)
        );
    }

    @Test
    void 캐시에_없는_유저_조회시_예외가_발생한다() {
        // given
        String nonExistentEmail = "nonexistent@moment.com";

        // when & then
        MomentException thrown = assertThrows(MomentException.class, () -> {
            pendingUserCache.getPendingUser(nonExistentEmail);
        });

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PENDING_USER_NOT_FOUND);
    }

    @Test
    void 캐시에_저장된_유저_정보를_삭제한다() {
        // given
        String email = "test@moment.com";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        PendingUser pendingUser = new PendingUser(email, googleUserInfo);
        pendingUserCache.save(pendingUser);

        // when
        pendingUserCache.remove(email);

        // then
        assertThrows(MomentException.class, () -> {
            pendingUserCache.getPendingUser(email);
        });
    }

    @Test
    void 만료_시간이_지나면_캐시에서_자동으로_삭제된다() throws InterruptedException {
        // given
        pendingUserCache = new PendingUserCache(10, TimeUnit.MILLISECONDS);
        String email = "test@moment.com";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo("sub", "name", "givenName", "picture", email, true);
        PendingUser pendingUser = new PendingUser(email, googleUserInfo);

        // when
        pendingUserCache.save(pendingUser);
        Thread.sleep(20); // 만료시간보다 길게 대기

        // then
        assertThrows(MomentException.class, () -> {
            pendingUserCache.getPendingUser(email);
        });
    }
}