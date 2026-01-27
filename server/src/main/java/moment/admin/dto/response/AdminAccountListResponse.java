package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminAccountListResponse(
    Long id,
    String email,
    String name,
    AdminRole role,
    boolean isBlocked,
    LocalDateTime createdAt,
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
