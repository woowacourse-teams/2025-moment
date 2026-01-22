package moment.group.dto.event;

public record GroupJoinApprovedEvent(
    Long groupId,
    Long userId,
    Long memberId
) {}
