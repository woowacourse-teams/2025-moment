package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.GroupMember;

@Schema(description = "그룹 소유자 정보")
public record AdminGroupOwnerInfo(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 내 닉네임", example = "방장닉네임")
    String nickname,

    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "사용자 이메일", example = "owner@example.com")
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
