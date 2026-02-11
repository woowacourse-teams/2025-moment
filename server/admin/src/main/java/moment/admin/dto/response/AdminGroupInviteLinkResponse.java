package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.group.domain.GroupInviteLink;

@Schema(description = "초대 링크 상세 응답")
public record AdminGroupInviteLinkResponse(
    @Schema(description = "초대 코드", example = "abc123xyz")
    String code,

    @Schema(description = "전체 초대 URL", example = "https://moment.com/invite/abc123xyz")
    String fullUrl,

    @Schema(description = "만료 일시", example = "2024-02-15T14:30:00")
    LocalDateTime expiresAt,

    @Schema(description = "활성화 여부", example = "true")
    boolean isActive,

    @Schema(description = "만료 여부", example = "false")
    boolean isExpired,

    @Schema(description = "생성 일시", example = "2024-01-15T14:30:00")
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
