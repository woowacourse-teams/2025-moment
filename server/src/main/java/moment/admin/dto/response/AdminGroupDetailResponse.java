package moment.admin.dto.response;

import java.time.LocalDateTime;

public record AdminGroupDetailResponse(
    Long groupId,
    String name,
    String description,
    int memberCount,
    int pendingMemberCount,
    int momentCount,
    int commentCount,
    AdminGroupOwnerInfo owner,
    AdminInviteLinkInfo inviteLink,
    LocalDateTime createdAt,
    LocalDateTime deletedAt,
    boolean isDeleted
) {
}
