package moment.admin.dto.response;

import moment.group.domain.GroupMember;

public record AdminCommentAuthorInfo(
    Long memberId,
    String groupNickname,
    Long userId,
    String userEmail,
    String userNickname
) {
    public static AdminCommentAuthorInfo from(GroupMember member) {
        return new AdminCommentAuthorInfo(
            member.getId(),
            member.getNickname(),
            member.getUser().getId(),
            member.getUser().getEmail(),
            member.getUser().getNickname()
        );
    }
}
