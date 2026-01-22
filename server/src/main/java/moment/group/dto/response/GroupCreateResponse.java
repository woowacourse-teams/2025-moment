package moment.group.dto.response;

import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;

public record GroupCreateResponse(
    Long groupId,
    String name,
    String description,
    Long memberId,
    String nickname,
    String inviteCode
) {
    public static GroupCreateResponse from(Group group, GroupMember ownerMember, GroupInviteLink inviteLink) {
        return new GroupCreateResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            ownerMember.getId(),
            ownerMember.getNickname(),
            inviteLink.getCode()
        );
    }
}
