package moment.admin.global.util;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSessionManager {

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_ROLE_KEY = "ADMIN_ROLE";

    private final AdminSessionRepository adminSessionRepository;
    private final AdminRepository adminRepository;

    @Value("${admin.session.timeout}")
    private int sessionTimeout;

    public void setAuth(HttpSession session, Long adminId, AdminRole role) {
        session.setAttribute(ADMIN_SESSION_KEY, adminId);
        session.setAttribute(ADMIN_ROLE_KEY, role);
        session.setMaxInactiveInterval(sessionTimeout);
    }

    public void validateAuthorized(HttpSession session) {
        if (session == null || session.getAttribute(ADMIN_SESSION_KEY) == null
                || session.getAttribute(ADMIN_ROLE_KEY) == null) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public Long getId(HttpSession session) {
        try {
            Object adminId = session.getAttribute(ADMIN_SESSION_KEY);
            if (adminId == null) {
                throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
            }
            return (Long) adminId;
        } catch (ClassCastException e) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public AdminRole getRole(HttpSession session) {
        try {
            Object role = session.getAttribute(ADMIN_ROLE_KEY);
            if (role == null) {
                throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
            }
            return (AdminRole) role;
        } catch (ClassCastException e) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public boolean isSuperAdmin(HttpSession session) {
        return getRole(session).isSuperAdmin();
    }

    public boolean canManageAdmins(HttpSession session) {
        return isSuperAdmin(session);
    }

    public void invalidate(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    // ===== 새로 추가된 메서드 =====

    /**
     * 로그인 시 세션을 DB에 등록
     * @param session HTTP 세션
     * @param adminId 관리자 ID
     * @param role 관리자 역할
     * @param ipAddress 클라이언트 IP 주소
     * @param userAgent User-Agent 헤더
     */
    public void registerSession(HttpSession session, Long adminId, AdminRole role,
                                String ipAddress, String userAgent) {
        // 세션 속성 설정 (기존 로직)
        setAuth(session, adminId, role);

        // DB에 세션 정보 저장
        String sessionId = session.getId();
        AdminSession adminSession = new AdminSession(adminId, sessionId, ipAddress, userAgent);
        adminSessionRepository.save(adminSession);

        log.info("Admin session registered: sessionId={}, adminId={}, ip={}", sessionId, adminId, ipAddress);
    }

    /**
     * 마지막 활동 시간 갱신
     * @param sessionId HTTP 세션 ID
     */
    public void updateLastAccessTime(String sessionId) {
        adminSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.updateLastAccessTime();
            adminSessionRepository.save(session);
        });
    }

    /**
     * 특정 관리자의 모든 활성 세션 무효화 (차단 시 사용)
     * @param adminId 관리자 ID
     */
    public void invalidateAllSessionsForAdmin(Long adminId) {
        List<AdminSession> activeSessions = adminSessionRepository.findByAdminIdAndLogoutTimeIsNull(adminId);

        activeSessions.forEach(session -> {
            session.markLoggedOut();
            adminSessionRepository.save(session);
            log.info("Admin session invalidated: sessionId={}, adminId={}", session.getSessionId(), adminId);
        });

        log.info("All active sessions invalidated for adminId={}, count={}", adminId, activeSessions.size());
    }

    /**
     * 특정 세션 무효화 (강제 로그아웃 시 사용)
     * @param sessionId HTTP 세션 ID
     */
    public void invalidateSessionById(String sessionId) {
        adminSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            if (session.isActive()) {
                session.markLoggedOut();
                adminSessionRepository.save(session);
                log.info("Admin session invalidated: sessionId={}, adminId={}", sessionId, session.getAdminId());
            }
        });
    }

    /**
     * 세션이 DB에서 활성 상태인지 확인
     * @param sessionId HTTP 세션 ID
     * @return DB에 세션이 존재하고 활성 상태이면 true
     */
    public boolean isSessionActiveInDb(String sessionId) {
        return adminSessionRepository.findBySessionId(sessionId)
                .map(AdminSession::isActive)
                .orElse(false);
    }

    /**
     * DB에서 세션을 조회하여 HTTP 세션을 복원
     * 서버 재시작 후에도 브라우저 쿠키의 세션 ID로 로그인 상태 유지
     *
     * @param session HTTP 세션
     * @return 복원 성공 시 true, 실패 시 false
     */
    public boolean restoreSessionFromDb(HttpSession session) {
        String sessionId = session.getId();

        // DB에서 세션 조회
        return adminSessionRepository.findBySessionId(sessionId)
                .filter(AdminSession::isActive)  // 활성 세션만
                .flatMap(adminSession -> {
                    Long adminId = adminSession.getAdminId();

                    // Admin 엔티티 조회하여 role 가져오기
                    return adminRepository.findById(adminId)
                            .filter(admin -> !admin.isBlocked())  // 차단되지 않은 관리자만
                            .map(admin -> {
                                try {
                                    // HTTP 세션에 인증 정보 복원
                                    setAuth(session, admin.getId(), admin.getRole());

                                    log.info("✅ Session restored from DB: adminId={}, role={}, sessionId={}",
                                            admin.getId(), admin.getRole(), sessionId);
                                    return true;
                                } catch (Exception e) {
                                    log.error("Failed to restore session from DB: sessionId={}", sessionId, e);
                                    return false;
                                }
                            });
                })
                .orElseGet(() -> {
                    log.info("❌ Failed to restore session from DB: session not found or inactive, sessionId={}", sessionId);
                    return false;
                });
    }
}
