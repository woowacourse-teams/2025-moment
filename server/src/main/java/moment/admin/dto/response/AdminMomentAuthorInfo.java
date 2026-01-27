package moment.admin.dto.response;

import moment.group.domain.GroupMember;

public record AdminMomentAuthorInfo(
    Long memberId,
    String groupNickname,
    Long userId,
    String userEmail,
    String userNickname
) {
    public static AdminMomentAuthorInfo from(GroupMember member) {
        return new AdminMomentAuthorInfo(
            member.getId(),
            member.getNickname(),
            member.getUser().getId(),
            member.getUser().getEmail(),
            member.getUser().getNickname()
        );
    }
}
