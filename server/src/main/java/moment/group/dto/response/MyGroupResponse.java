package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;

@Schema(description = "내 그룹 응답")
public record MyGroupResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "내 멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "내 닉네임", example = "홍길동")
    String myNickname,

    @Schema(description = "소유자 여부", example = "true")
    boolean isOwner,

    @Schema(description = "멤버 수", example = "10")
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
