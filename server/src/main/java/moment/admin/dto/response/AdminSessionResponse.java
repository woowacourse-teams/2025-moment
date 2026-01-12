package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;

import java.time.LocalDateTime;

/**
 * 세션 목록 응답 DTO
 */
public record AdminSessionResponse(
        Long id,
        Long adminId,
        String adminName,
        String adminEmail,
        AdminRole adminRole,
        String sessionId,
        LocalDateTime loginTime,
        LocalDateTime lastAccessTime,
        String ipAddress,
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
