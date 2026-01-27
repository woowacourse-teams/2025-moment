package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminLoginResponse(
    Long id,
    String email,
    String name,
    AdminRole role
) {
    public static AdminLoginResponse from(Admin admin) {
        return new AdminLoginResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole()
        );
    }
}
