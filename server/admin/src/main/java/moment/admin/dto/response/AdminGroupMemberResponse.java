package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.group.domain.GroupMember;

@Schema(description = "그룹 멤버 정보")
public record AdminGroupMemberResponse(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 내 닉네임", example = "멤버닉네임")
    String nickname,

    @Schema(description = "역할 (OWNER, MEMBER)", example = "MEMBER")
    String role,

    @Schema(description = "상태 (APPROVED, PENDING)", example = "APPROVED")
    String status,

    @Schema(description = "가입 일시", example = "2024-01-20T10:00:00")
    LocalDateTime joinedAt,

    @Schema(description = "사용자 정보")
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
