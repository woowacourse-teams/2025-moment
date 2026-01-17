package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminSession;

import java.time.LocalDateTime;

/**
 * 세션 히스토리 응답 DTO
 */
public record AdminSessionHistoryResponse(
        Long id,
        String adminName,
        String adminEmail,
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        String ipAddress,
        String sessionStatus
) {
    public static AdminSessionHistoryResponse from(AdminSession session, Admin admin) {
        String status = determineSessionStatus(session);
        return new AdminSessionHistoryResponse(
                session.getId(),
                admin.getName(),
                admin.getEmail(),
                session.getLoginTime(),
                session.getLogoutTime(),
                session.getIpAddress(),
                status
        );
    }

    private static String determineSessionStatus(AdminSession session) {
        if (session.getLogoutTime() == null) {
            return "ACTIVE";
        }
        // 세션 만료 여부는 별도 플래그가 없으므로 logoutTime 존재 여부로 판단
        return "LOGGED_OUT";
    }

    /**
     * 상태에 따른 배지 CSS 클래스 반환
     */
    public String getStatusBadgeClass() {
        return switch (sessionStatus) {
            case "ACTIVE" -> "bg-green-100 text-green-800";
            case "LOGGED_OUT" -> "bg-gray-100 text-gray-800";
            case "FORCED_LOGOUT" -> "bg-red-100 text-red-800";
            case "EXPIRED" -> "bg-yellow-100 text-yellow-800";
            default -> "bg-gray-100 text-gray-800";
        };
    }

    /**
     * 상태 한글 표시
     */
    public String getStatusLabel() {
        return switch (sessionStatus) {
            case "ACTIVE" -> "활성";
            case "LOGGED_OUT" -> "로그아웃";
            case "FORCED_LOGOUT" -> "강제 로그아웃";
            case "EXPIRED" -> "만료";
            default -> "알 수 없음";
        };
    }
}
