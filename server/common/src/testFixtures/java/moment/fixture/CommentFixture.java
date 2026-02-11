package moment.fixture;

import java.lang.reflect.Field;
import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public class CommentFixture {

    public static Comment createComment(User commenter, Long momentId) {
        return new Comment("테스트 댓글 내용", commenter, momentId);
    }

    public static Comment createCommentWithId(Long id, User commenter, Long momentId) {
        Comment comment = new Comment("테스트 댓글 내용", commenter, momentId);
        setId(comment, id);
        return comment;
    }

    public static Comment createCommentInGroup(Moment moment, User commenter, GroupMember member) {
        return new Comment(moment, commenter, member, "그룹 내 댓글 내용");
    }

    public static Comment createCommentInGroupWithId(Long id, Moment moment, User commenter, GroupMember member) {
        Comment comment = new Comment(moment, commenter, member, "그룹 내 댓글 내용");
        setId(comment, id);
        return comment;
    }

    private static void setId(Comment comment, Long id) {
        try {
            Field idField = Comment.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(comment, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
