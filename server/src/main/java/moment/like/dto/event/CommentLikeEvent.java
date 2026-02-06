package moment.like.dto.event;

public record CommentLikeEvent(
    Long commentId,
    Long commentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId
) {}
