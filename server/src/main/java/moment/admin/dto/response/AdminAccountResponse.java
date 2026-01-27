package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminAccountResponse(
    Long id,
    String email,
    String name,
    AdminRole role,
    LocalDateTime createdAt
) {
    public static AdminAccountResponse from(Admin admin) {
        return new AdminAccountResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.getCreatedAt()
        );
    }
}
