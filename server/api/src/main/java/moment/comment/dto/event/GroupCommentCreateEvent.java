package moment.comment.dto.event;

public record GroupCommentCreateEvent(
    Long groupId,
    Long momentId,
    Long momentOwnerId,
    Long commentId,
    Long commenterId,
    String commenterNickname
) {}
