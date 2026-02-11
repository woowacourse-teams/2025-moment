package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.GroupMember;

@Schema(description = "모멘트 작성자 정보")
public record AdminMomentAuthorInfo(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 내 닉네임", example = "작성자닉네임")
    String groupNickname,

    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "사용자 이메일", example = "author@example.com")
    String userEmail,

    @Schema(description = "사용자 닉네임", example = "사용자닉네임")
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
