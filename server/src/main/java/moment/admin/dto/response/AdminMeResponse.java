package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

@Schema(description = "현재 관리자 정보 응답")
public record AdminMeResponse(
    @Schema(description = "관리자 ID", example = "1")
    Long id,

    @Schema(description = "관리자 이메일", example = "admin@moment.com")
    String email,

    @Schema(description = "관리자 이름", example = "홍길동")
    String name,

    @Schema(description = "관리자 권한", example = "SUPER_ADMIN")
    AdminRole role,

    @Schema(description = "생성 일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt
) {
    public static AdminMeResponse from(Admin admin) {
        return new AdminMeResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.getCreatedAt()
        );
    }
}
