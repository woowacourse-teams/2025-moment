package moment.admin.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.dto.response.AdminResponse;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.session.AdminSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 관리 애플리케이션 서비스
 * 여러 도메인 서비스를 조율하여 복잡한 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminManagementApplicationService {

    private final AdminService adminService;
    private final AdminSessionService adminSessionService;
    private final AdminSessionManager sessionManager;

    /**
     * 관리자 차단 및 모든 세션 무효화
     * @param currentAdminId 현재 로그인한 관리자 ID (차단 실행자)
     * @param targetAdminId 차단 대상 관리자 ID
     */
    @Transactional
    public void blockAdminAndInvalidateSessions(Long currentAdminId, Long targetAdminId) {
        // 1. 자기 자신 차단 방지
        adminService.validateNotSelfBlock(currentAdminId, targetAdminId);

        // 2. 관리자 차단 (Soft Delete)
        adminService.blockAdmin(targetAdminId);

        // 3. 해당 관리자의 모든 활성 세션 무효화
        sessionManager.invalidateAllSessionsForAdmin(targetAdminId);

        log.info("Admin blocked and sessions invalidated: targetAdminId={}, executor={}", targetAdminId, currentAdminId);
    }

    /**
     * 관리자 차단 해제
     * @param adminId 차단 해제할 관리자 ID
     */
    @Transactional
    public void unblockAdmin(Long adminId) {
        adminService.unblockAdmin(adminId);
        log.info("Admin unblocked: adminId={}", adminId);
    }

    /**
     * 모든 관리자 조회 (상태 포함)
     * @param pageable 페이징 정보
     * @return 관리자 응답 페이지
     */
    public Page<AdminResponse> getAllAdminsWithStatus(Pageable pageable) {
        Page<Admin> admins = adminService.getAllAdmins(pageable);
        return admins.map(AdminResponse::from);
    }

    /**
     * 모든 활성 세션 조회
     * @return 활성 세션 응답 목록
     */
    public List<AdminSessionResponse> getAllActiveSessions() {
        return adminSessionService.getAllActiveSessions();
    }

    /**
     * 특정 세션 강제 로그아웃
     * @param sessionId HTTP 세션 ID
     */
    @Transactional
    public void forceLogoutSession(String sessionId) {
        sessionManager.invalidateSessionById(sessionId);
        log.info("Session force logged out: sessionId={}", sessionId);
    }

    /**
     * 특정 관리자의 모든 세션 강제 로그아웃
     * @param currentAdminId 현재 로그인한 관리자 ID
     * @param targetAdminId 강제 로그아웃 대상 관리자 ID
     */
    @Transactional
    public void forceLogoutAllSessionsForAdmin(Long currentAdminId, Long targetAdminId) {
        // 자기 자신 차단 방지
        adminService.validateNotSelfBlock(currentAdminId, targetAdminId);

        // 모든 세션 무효화
        sessionManager.invalidateAllSessionsForAdmin(targetAdminId);

        log.info("All sessions force logged out for adminId={}, executor={}", targetAdminId, currentAdminId);
    }
}
