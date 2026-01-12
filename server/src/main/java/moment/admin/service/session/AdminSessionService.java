package moment.admin.service.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminSession;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminSessionService {

    private final AdminSessionRepository adminSessionRepository;
    private final AdminRepository adminRepository;

    /**
     * 모든 활성 세션 조회 (관리자 정보 포함)
     * @return 세션 목록 (AdminSessionResponse)
     */
    public List<AdminSessionResponse> getAllActiveSessions() {
        List<AdminSession> activeSessions = adminSessionRepository.findAllByLogoutTimeIsNullOrderByLoginTimeDesc();

        return activeSessions.stream()
                .map(session -> {
                    Admin admin = adminRepository.findById(session.getAdminId())
                            .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));
                    return AdminSessionResponse.from(session, admin);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 관리자의 활성 세션 목록 조회
     * @param adminId 관리자 ID
     * @return 세션 목록
     */
    public List<AdminSessionResponse> getActiveSessionsByAdminId(Long adminId) {
        List<AdminSession> activeSessions = adminSessionRepository.findByAdminIdAndLogoutTimeIsNull(adminId);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));

        return activeSessions.stream()
                .map(session -> AdminSessionResponse.from(session, admin))
                .collect(Collectors.toList());
    }

    /**
     * 세션 ID로 세션 조회
     * @param sessionId HTTP 세션 ID
     * @return AdminSession
     */
    public AdminSession getSessionBySessionId(String sessionId) {
        return adminSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_SESSION_NOT_FOUND));
    }
}
