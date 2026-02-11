package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 통계 응답")
public record AdminGroupStatsResponse(
    @Schema(description = "전체 그룹 수", example = "150")
    long totalGroups,

    @Schema(description = "활성 그룹 수", example = "120")
    long activeGroups,

    @Schema(description = "삭제된 그룹 수", example = "30")
    long deletedGroups,

    @Schema(description = "전체 멤버 수", example = "1500")
    long totalMembers,

    @Schema(description = "전체 모멘트 수", example = "5000")
    long totalMoments,

    @Schema(description = "오늘 생성된 그룹 수", example = "5")
    long todayCreatedGroups
) {
}
