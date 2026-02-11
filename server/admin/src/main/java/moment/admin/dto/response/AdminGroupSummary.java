package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "그룹 요약 정보")
public record AdminGroupSummary(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹명", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "멤버 수", example = "25")
    int memberCount,

    @Schema(description = "모멘트 수", example = "150")
    int momentCount,

    @Schema(description = "그룹 소유자 정보")
    AdminGroupOwnerInfo owner,

    @Schema(description = "생성 일시", example = "2024-01-15T14:30:00")
    LocalDateTime createdAt,

    @Schema(description = "삭제 일시 (삭제된 경우)", example = "2024-02-01T10:00:00")
    LocalDateTime deletedAt,

    @Schema(description = "삭제 여부", example = "false")
    boolean isDeleted
) {
}
