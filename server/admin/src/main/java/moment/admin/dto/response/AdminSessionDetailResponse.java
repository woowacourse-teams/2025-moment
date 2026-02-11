package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;
import moment.admin.global.util.UserAgentParser;

import java.time.LocalDateTime;

/**
 * 세션 상세 정보 응답 DTO
 */
@Schema(description = "세션 상세 정보 응답")
public record AdminSessionDetailResponse(
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

        @Schema(description = "User-Agent (원본)", example = "Mozilla/5.0...")
        String userAgent,

        @Schema(description = "브라우저 정보", example = "Chrome 120")
        String browser,

        @Schema(description = "운영체제 정보", example = "Windows 11")
        String os,

        @Schema(description = "디바이스 타입", example = "Desktop")
        String deviceType,

        @Schema(description = "로그아웃 일시", example = "2024-01-15T12:00:00")
        LocalDateTime logoutTime,

        @Schema(description = "세션 활성 상태", example = "true")
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
