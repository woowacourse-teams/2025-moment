package moment.admin.dto.response;

public record AdminGroupStatsResponse(
    long totalGroups,
    long activeGroups,
    long deletedGroups,
    long totalMembers,
    long totalMoments,
    long todayCreatedGroups
) {
}
