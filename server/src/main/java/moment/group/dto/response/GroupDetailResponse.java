package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;

@Schema(description = "그룹 상세 응답")
public record GroupDetailResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "내 멤버 ID", example = "1")
    Long myMemberId,

    @Schema(description = "내 닉네임", example = "홍길동")
    String myNickname,

    @Schema(description = "소유자 여부", example = "true")
    boolean isOwner,

    @Schema(description = "멤버 수", example = "10")
    long memberCount,

    @Schema(description = "멤버 목록")
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
