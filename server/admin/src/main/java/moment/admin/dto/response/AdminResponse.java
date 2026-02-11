package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

import java.time.LocalDateTime;

/**
 * 관리자 목록 응답 DTO
 */
public record AdminResponse(
        Long id,
        String email,
        String name,
        AdminRole role,
        LocalDateTime createdAt,
        LocalDateTime deletedAt,  // 차단 여부 확인용
        boolean isBlocked
) {
    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getName(),
                admin.getRole(),
                admin.getCreatedAt(),
                admin.getDeletedAt(),
                admin.getDeletedAt() != null
        );
    }
}
