package moment.fixture;

import moment.block.domain.UserBlock;
import moment.user.domain.User;

public class UserBlockFixture {

    public static UserBlock createUserBlock(User blocker, User blockedUser) {
        return new UserBlock(blocker, blockedUser);
    }

    public static UserBlock createUserBlockWithId(Long id, User blocker, User blockedUser) {
        UserBlock userBlock = new UserBlock(blocker, blockedUser);
        setId(userBlock, id);
        return userBlock;
    }

    private static void setId(UserBlock userBlock, Long id) {
        try {
            java.lang.reflect.Field idField = UserBlock.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userBlock, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
