package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.group.domain.GroupInviteLink;

public record AdminInviteLinkInfo(
    String code,
    LocalDateTime expiresAt,
    boolean isActive,
    boolean isExpired
) {
    public static AdminInviteLinkInfo from(GroupInviteLink inviteLink) {
        if (inviteLink == null) {
            return null;
        }
        boolean expired = LocalDateTime.now().isAfter(inviteLink.getExpiredAt());
        return new AdminInviteLinkInfo(
            inviteLink.getCode(),
            inviteLink.getExpiredAt(),
            inviteLink.isActive(),
            expired
        );
    }
}
