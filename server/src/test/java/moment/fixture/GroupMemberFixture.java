package moment.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;
import moment.user.domain.User;

public class GroupMemberFixture {

    public static GroupMember createOwnerMember(Group group, User user, String nickname) {
        return GroupMember.createOwner(group, user, nickname);
    }

    public static GroupMember createApprovedMember(Group group, User user, String nickname) {
        GroupMember member = GroupMember.createPendingMember(group, user, nickname);
        member.approve();
        return member;
    }

    public static GroupMember createPendingMember(Group group, User user, String nickname) {
        return GroupMember.createPendingMember(group, user, nickname);
    }

    public static GroupMember createDeletedMember(Group group, User user, String nickname) {
        GroupMember member = GroupMember.createPendingMember(group, user, nickname);
        setDeletedAt(member, LocalDateTime.now().minusDays(1));
        return member;
    }

    public static GroupMember createMemberWithId(Long id, Group group, User user, String nickname) {
        GroupMember member = GroupMember.createPendingMember(group, user, nickname);
        member.approve();
        setId(member, id);
        return member;
    }

    private static void setId(GroupMember member, Long id) {
        try {
            Field idField = GroupMember.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

    private static void setDeletedAt(GroupMember member, LocalDateTime deletedAt) {
        try {
            Field deletedAtField = GroupMember.class.getDeclaredField("deletedAt");
            deletedAtField.setAccessible(true);
            deletedAtField.set(member, deletedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set deletedAt via reflection", e);
        }
    }
}
