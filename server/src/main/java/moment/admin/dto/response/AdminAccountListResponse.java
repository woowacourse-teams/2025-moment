package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

@Schema(description = "관리자 계정 목록 응답")
public record AdminAccountListResponse(
    @Schema(description = "관리자 ID", example = "1")
    Long id,

    @Schema(description = "관리자 이메일", example = "admin@moment.com")
    String email,

    @Schema(description = "관리자 이름", example = "홍길동")
    String name,

    @Schema(description = "관리자 권한", example = "ADMIN")
    AdminRole role,

    @Schema(description = "차단 여부", example = "false")
    boolean isBlocked,

    @Schema(description = "생성 일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "삭제 일시 (Soft Delete)", example = "null")
    LocalDateTime deletedAt
) {
    public static AdminAccountListResponse from(Admin admin) {
        return new AdminAccountListResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.isBlocked(),
            admin.getCreatedAt(),
            admin.getDeletedAt()
        );
    }
}
