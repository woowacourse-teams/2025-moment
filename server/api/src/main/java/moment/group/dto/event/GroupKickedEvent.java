package moment.group.dto.event;

public record GroupKickedEvent(
    Long groupId,
    Long kickedUserId,
    Long memberId
) {}
