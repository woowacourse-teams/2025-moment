package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.GroupMember;

@Schema(description = "댓글 작성자 정보")
public record AdminCommentAuthorInfo(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 내 닉네임", example = "댓글작성자")
    String groupNickname,

    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "사용자 이메일", example = "commenter@example.com")
    String userEmail,

    @Schema(description = "사용자 닉네임", example = "사용자닉네임")
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
