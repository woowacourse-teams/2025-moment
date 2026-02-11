package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;

@Schema(description = "그룹 생성 응답")
public record GroupCreateResponse(
    @Schema(description = "생성된 그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "소유자 닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "초대 코드", example = "abc123xyz")
    String inviteCode
) {
    public static GroupCreateResponse from(Group group, GroupMember ownerMember, GroupInviteLink inviteLink) {
        return new GroupCreateResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            ownerMember.getId(),
            ownerMember.getNickname(),
            inviteLink.getCode()
        );
    }
}
