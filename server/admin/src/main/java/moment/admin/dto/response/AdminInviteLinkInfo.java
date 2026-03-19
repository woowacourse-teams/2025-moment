package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.group.domain.GroupInviteLink;

@Schema(description = "초대 링크 정보")
public record AdminInviteLinkInfo(
    @Schema(description = "초대 코드", example = "abc123xyz")
    String code,

    @Schema(description = "만료 일시", example = "2024-02-15T14:30:00")
    LocalDateTime expiresAt,

    @Schema(description = "활성화 여부", example = "true")
    boolean isActive,

    @Schema(description = "만료 여부", example = "false")
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
