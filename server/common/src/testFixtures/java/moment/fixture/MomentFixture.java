package moment.fixture;

import java.lang.reflect.Field;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public class MomentFixture {

    public static Moment createMoment(User momenter) {
        return new Moment("테스트 모멘트 내용", momenter);
    }

    public static Moment createMomentWithId(Long id, User momenter) {
        Moment moment = new Moment("테스트 모멘트 내용", momenter);
        setId(moment, id);
        return moment;
    }

    public static Moment createMomentInGroup(User momenter, Group group, GroupMember member) {
        return new Moment(momenter, group, member, "그룹 내 모멘트 내용");
    }

    public static Moment createMomentInGroupWithId(Long id, User momenter, Group group, GroupMember member) {
        Moment moment = new Moment(momenter, group, member, "그룹 내 모멘트 내용");
        setId(moment, id);
        return moment;
    }

    private static void setId(Moment moment, Long id) {
        try {
            Field idField = Moment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(moment, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
