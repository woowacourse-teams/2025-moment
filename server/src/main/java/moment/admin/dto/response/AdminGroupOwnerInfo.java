package moment.admin.dto.response;

import moment.group.domain.GroupMember;

public record AdminGroupOwnerInfo(
    Long memberId,
    String nickname,
    Long userId,
    String userEmail
) {
    public static AdminGroupOwnerInfo from(GroupMember owner) {
        return new AdminGroupOwnerInfo(
            owner.getId(),
            owner.getNickname(),
            owner.getUser().getId(),
            owner.getUser().getEmail()
        );
    }
}
