package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.group.domain.GroupInviteLink;

public record AdminGroupInviteLinkResponse(
    String code,
    String fullUrl,
    LocalDateTime expiresAt,
    boolean isActive,
    boolean isExpired,
    LocalDateTime createdAt
) {
    public static AdminGroupInviteLinkResponse from(GroupInviteLink inviteLink, String baseUrl) {
        boolean expired = LocalDateTime.now().isAfter(inviteLink.getExpiredAt());
        return new AdminGroupInviteLinkResponse(
            inviteLink.getCode(),
            baseUrl + "/invite/" + inviteLink.getCode(),
            inviteLink.getExpiredAt(),
            inviteLink.isActive(),
            expired,
            inviteLink.getCreatedAt()
        );
    }
}
