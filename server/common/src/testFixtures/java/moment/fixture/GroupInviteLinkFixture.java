package moment.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;

public class GroupInviteLinkFixture {

    public static GroupInviteLink createValidLink(Group group) {
        return new GroupInviteLink(group, 7);
    }

    public static GroupInviteLink createValidLinkWithCode(Group group, String code) {
        GroupInviteLink link = new GroupInviteLink(group, 7);
        setCode(link, code);
        return link;
    }

    public static GroupInviteLink createExpiredLink(Group group) {
        GroupInviteLink link = new GroupInviteLink(group, 7);
        setExpiredAt(link, LocalDateTime.now().minusDays(1));
        return link;
    }

    public static GroupInviteLink createInactiveLink(Group group) {
        GroupInviteLink link = new GroupInviteLink(group, 7);
        link.deactivate();
        return link;
    }

    private static void setCode(GroupInviteLink link, String code) {
        try {
            Field codeField = GroupInviteLink.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(link, code);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set code via reflection", e);
        }
    }

    private static void setExpiredAt(GroupInviteLink link, LocalDateTime expiredAt) {
        try {
            Field expiredAtField = GroupInviteLink.class.getDeclaredField("expiredAt");
            expiredAtField.setAccessible(true);
            expiredAtField.set(link, expiredAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set expiredAt via reflection", e);
        }
    }
}
