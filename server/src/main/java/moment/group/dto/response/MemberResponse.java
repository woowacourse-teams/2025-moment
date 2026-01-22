package moment.group.dto.response;

import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;

public record MemberResponse(
    Long memberId,
    Long userId,
    String nickname,
    MemberRole role,
    MemberStatus status
) {
    public static MemberResponse from(GroupMember member) {
        return new MemberResponse(
            member.getId(),
            member.getUser().getId(),
            member.getNickname(),
            member.getRole(),
            member.getStatus()
        );
    }
}
