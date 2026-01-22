package moment.group.dto.response;

import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;

public record GroupJoinResponse(
    Long memberId,
    Long groupId,
    String nickname,
    MemberStatus status
) {
    public static GroupJoinResponse from(GroupMember member) {
        return new GroupJoinResponse(
            member.getId(),
            member.getGroup().getId(),
            member.getNickname(),
            member.getStatus()
        );
    }
}
