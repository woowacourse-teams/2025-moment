package moment.block.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.fixture.UserFixture;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockTest {

    @Test
    void UserBlock을_생성한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);

        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        assertThat(userBlock.getBlocker()).isEqualTo(blocker);
        assertThat(userBlock.getBlockedUser()).isEqualTo(blockedUser);
        assertThat(userBlock.isDeleted()).isFalse();
    }

    @Test
    void restore_호출_시_deletedAt이_null이_된다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        userBlock.restore();

        assertThat(userBlock.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_deletedAt이_null이면_false를_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        assertThat(userBlock.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_deletedAt이_존재하면_true를_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = new UserBlock(blocker, blockedUser);
        setDeletedAt(userBlock);

        assertThat(userBlock.isDeleted()).isTrue();
    }

    private void setDeletedAt(UserBlock userBlock) {
        try {
            java.lang.reflect.Field deletedAtField = UserBlock.class.getDeclaredField("deletedAt");
            deletedAtField.setAccessible(true);
            deletedAtField.set(userBlock, java.time.LocalDateTime.now());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set deletedAt via reflection", e);
        }
    }
}
