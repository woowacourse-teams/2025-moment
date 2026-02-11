package moment.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import moment.group.domain.GroupMember;
import moment.like.domain.MomentLike;
import moment.moment.domain.Moment;

public class MomentLikeFixture {

    public static MomentLike createMomentLike(Moment moment, GroupMember member) {
        return new MomentLike(moment, member);
    }

    public static MomentLike createDeletedMomentLike(Moment moment, GroupMember member) {
        MomentLike like = new MomentLike(moment, member);
        setDeletedAt(like, LocalDateTime.now().minusDays(1));
        return like;
    }

    private static void setDeletedAt(MomentLike like, LocalDateTime deletedAt) {
        try {
            Field deletedAtField = MomentLike.class.getDeclaredField("deletedAt");
            deletedAtField.setAccessible(true);
            deletedAtField.set(like, deletedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set deletedAt via reflection", e);
        }
    }
}
