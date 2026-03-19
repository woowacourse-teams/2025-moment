package moment.group.dto.event;

public record GroupJoinRequestEvent(
    Long groupId,
    Long ownerId,
    Long memberId,
    String nickname
) {}
