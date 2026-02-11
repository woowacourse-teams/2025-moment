package moment.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.group.domain.Group;

@Schema(description = "초대 정보 응답")
public record InviteInfoResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "멤버 수", example = "10")
    long memberCount
) {
    public static InviteInfoResponse from(Group group, long memberCount) {
        return new InviteInfoResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            memberCount
        );
    }
}
