package moment.group.dto.response;

import moment.group.domain.Group;

public record InviteInfoResponse(
    Long groupId,
    String name,
    String description,
    long memberCount
) {
    public static InviteInfoResponse from(Group group, long memberCount) {
        return new InviteInfoResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            memberCount
        );
    }
}
