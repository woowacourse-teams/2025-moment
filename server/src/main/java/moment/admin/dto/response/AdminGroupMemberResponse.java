package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.group.domain.GroupMember;

public record AdminGroupMemberResponse(
    Long memberId,
    String nickname,
    String role,
    String status,
    LocalDateTime joinedAt,
    AdminMemberUserInfo user
) {
    public static AdminGroupMemberResponse from(GroupMember member) {
        return new AdminGroupMemberResponse(
            member.getId(),
            member.getNickname(),
            member.getRole().name(),
            member.getStatus().name(),
            member.getCreatedAt(),
            AdminMemberUserInfo.from(member.getUser())
        );
    }
}
