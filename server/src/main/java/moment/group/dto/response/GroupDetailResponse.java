package moment.group.dto.response;

import java.util.List;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;

public record GroupDetailResponse(
    Long groupId,
    String name,
    String description,
    Long myMemberId,
    String myNickname,
    boolean isOwner,
    long memberCount,
    List<MemberResponse> members
) {
    public static GroupDetailResponse from(Group group, GroupMember myMember, List<GroupMember> members, long memberCount) {
        return new GroupDetailResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            myMember.getId(),
            myMember.getNickname(),
            myMember.isOwner(),
            memberCount,
            members.stream().map(MemberResponse::from).toList()
        );
    }
}
