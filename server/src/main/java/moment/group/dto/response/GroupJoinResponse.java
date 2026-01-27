package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;

@Schema(description = "그룹 가입 응답")
public record GroupJoinResponse(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "닉네임", example = "새멤버")
    String nickname,

    @Schema(description = "상태", example = "PENDING")
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
