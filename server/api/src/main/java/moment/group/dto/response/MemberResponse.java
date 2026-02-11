package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;

@Schema(description = "멤버 응답")
public record MemberResponse(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "역할", example = "OWNER")
    MemberRole role,

    @Schema(description = "상태", example = "APPROVED")
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
