package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;
import moment.admin.global.util.UserAgentParser;

import java.time.LocalDateTime;

/**
 * 세션 상세 정보 응답 DTO
 */
public record AdminSessionDetailResponse(
        Long id,
        Long adminId,
        String adminName,
        String adminEmail,
        AdminRole adminRole,
        String sessionId,
        LocalDateTime loginTime,
        LocalDateTime lastAccessTime,
        String ipAddress,
        String userAgent,
        String browser,
        String os,
        String deviceType,
        LocalDateTime logoutTime,
        boolean isActive
) {
    public static AdminSessionDetailResponse from(AdminSession session, Admin admin) {
        UserAgentParser.ParsedUserAgent parsed = UserAgentParser.parse(session.getUserAgent());

        return new AdminSessionDetailResponse(
                session.getId(),
                session.getAdminId(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole(),
                session.getSessionId(),
                session.getLoginTime(),
                session.getLastAccessTime(),
                session.getIpAddress(),
                session.getUserAgent(),
                parsed.browser(),
                parsed.os(),
                parsed.deviceType(),
                session.getLogoutTime(),
                session.isActive()
        );
    }

    /**
     * 세션 ID 앞 8자만 반환 (UI 표시용)
     */
    public String getShortSessionId() {
        return sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId;
    }
}
