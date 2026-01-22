package moment.like.dto.event;

public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname
) {}
