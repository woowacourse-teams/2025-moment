package moment.group.dto.response;

import moment.group.domain.Group;
import moment.group.domain.GroupMember;

public record MyGroupResponse(
    Long groupId,
    String name,
    String description,
    Long memberId,
    String myNickname,
    boolean isOwner,
    long memberCount
) {
    public static MyGroupResponse from(Group group, GroupMember membership, long memberCount) {
        return new MyGroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            membership.getId(),
            membership.getNickname(),
            membership.isOwner(),
            memberCount
        );
    }
}
