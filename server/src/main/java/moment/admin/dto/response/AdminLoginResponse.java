package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

@Schema(description = "관리자 로그인 응답")
public record AdminLoginResponse(
    @Schema(description = "관리자 ID", example = "1")
    Long id,

    @Schema(description = "관리자 이메일", example = "admin@moment.com")
    String email,

    @Schema(description = "관리자 이름", example = "홍길동")
    String name,

    @Schema(description = "관리자 권한", example = "SUPER_ADMIN")
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
