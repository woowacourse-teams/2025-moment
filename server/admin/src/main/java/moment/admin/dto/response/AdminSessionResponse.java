package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;

import java.time.LocalDateTime;

/**
 * 세션 목록 응답 DTO
 */
@Schema(description = "활성 세션 목록 응답")
public record AdminSessionResponse(
        @Schema(description = "세션 레코드 ID", example = "1")
        Long id,

        @Schema(description = "관리자 ID", example = "1")
        Long adminId,

        @Schema(description = "관리자 이름", example = "홍길동")
        String adminName,

        @Schema(description = "관리자 이메일", example = "admin@moment.com")
        String adminEmail,

        @Schema(description = "관리자 권한", example = "SUPER_ADMIN")
        AdminRole adminRole,

        @Schema(description = "세션 ID", example = "ABC123DEF456...")
        String sessionId,

        @Schema(description = "로그인 일시", example = "2024-01-15T10:30:00")
        LocalDateTime loginTime,

        @Schema(description = "마지막 접근 일시", example = "2024-01-15T11:00:00")
        LocalDateTime lastAccessTime,

        @Schema(description = "접속 IP 주소", example = "192.168.1.1")
        String ipAddress,

        @Schema(description = "User-Agent", example = "Mozilla/5.0...")
        String userAgent
) {
    public static AdminSessionResponse from(AdminSession session, Admin admin) {
        return new AdminSessionResponse(
                session.getId(),
                session.getAdminId(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole(),
                session.getSessionId(),
                session.getLoginTime(),
                session.getLastAccessTime(),
                session.getIpAddress(),
                session.getUserAgent()
        );
    }

    /**
     * 세션 ID 앞 8자만 반환 (UI 표시용)
     */
    public String getShortSessionId() {
        return sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId;
    }
}
