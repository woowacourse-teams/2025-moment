package moment.fixture;

import java.lang.reflect.Field;
import moment.group.domain.Group;
import moment.user.domain.User;

public class GroupFixture {

    public static Group createGroup(User owner) {
        return new Group("테스트 그룹", "테스트 그룹 설명", owner);
    }

    public static Group createGroupWithId(Long id, User owner) {
        Group group = new Group("테스트 그룹", "테스트 그룹 설명", owner);
        setId(group, id);
        return group;
    }

    public static Group createGroupWithNameAndDescription(User owner, String name, String description) {
        return new Group(name, description, owner);
    }

    private static void setId(Group group, Long id) {
        try {
            Field idField = Group.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(group, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
