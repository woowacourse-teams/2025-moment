package moment.fixture;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;
import moment.like.domain.CommentLike;

public class CommentLikeFixture {

    public static CommentLike createCommentLike(Comment comment, GroupMember member) {
        return new CommentLike(comment, member);
    }

    public static CommentLike createDeletedCommentLike(Comment comment, GroupMember member) {
        CommentLike like = new CommentLike(comment, member);
        setDeletedAt(like, LocalDateTime.now().minusDays(1));
        return like;
    }

    private static void setDeletedAt(CommentLike like, LocalDateTime deletedAt) {
        try {
            Field deletedAtField = CommentLike.class.getDeclaredField("deletedAt");
            deletedAtField.setAccessible(true);
            deletedAtField.set(like, deletedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set deletedAt via reflection", e);
        }
    }
}
