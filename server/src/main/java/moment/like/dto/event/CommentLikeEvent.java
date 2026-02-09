package moment.like.dto.event;

import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;

public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId,
    Long likerUserId
) {
    public static CommentLikeEvent of(Comment comment, GroupMember member) {
        return new CommentLikeEvent(
            comment.getId(),
            comment.getCommenter().getId(),
            member.getId(),
            member.getNickname(),
            member.getGroup().getId(),
            member.getUser().getId()
        );
    }
}
