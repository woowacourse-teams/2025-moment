package moment.admin.dto.response;

import java.time.LocalDateTime;

public record AdminGroupSummary(
    Long groupId,
    String name,
    String description,
    int memberCount,
    int momentCount,
    AdminGroupOwnerInfo owner,
    LocalDateTime createdAt,
    LocalDateTime deletedAt,
    boolean isDeleted
) {
}
