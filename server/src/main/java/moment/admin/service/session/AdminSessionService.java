package moment.admin.service.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminSession;
import moment.admin.dto.response.AdminSessionDetailResponse;
import moment.admin.dto.response.AdminSessionHistoryResponse;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 세션 상세 정보 조회
     * @param id 세션 PK ID
     * @return AdminSessionDetailResponse
     */
    public AdminSessionDetailResponse getSessionDetail(Long id) {
        AdminSession session = adminSessionRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_SESSION_NOT_FOUND));

        Admin admin = adminRepository.findById(session.getAdminId())
                .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));

        return AdminSessionDetailResponse.from(session, admin);
    }

    /**
     * 필터링된 활성 세션 조회
     * @param adminId 관리자 ID (null이면 전체)
     * @param ipAddress IP 주소 (null이면 전체)
     * @return 필터링된 세션 목록
     */
    public List<AdminSessionResponse> getFilteredActiveSessions(Long adminId, String ipAddress) {
        List<AdminSession> activeSessions;

        if (adminId != null && ipAddress != null && !ipAddress.isBlank()) {
            activeSessions = adminSessionRepository.findByAdminIdAndIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(adminId, ipAddress);
        } else if (adminId != null) {
            activeSessions = adminSessionRepository.findByAdminIdAndLogoutTimeIsNullOrderByLoginTimeDesc(adminId);
        } else if (ipAddress != null && !ipAddress.isBlank()) {
            activeSessions = adminSessionRepository.findByIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(ipAddress);
        } else {
            activeSessions = adminSessionRepository.findAllByLogoutTimeIsNullOrderByLoginTimeDesc();
        }

        return activeSessions.stream()
                .map(session -> {
                    Admin admin = adminRepository.findById(session.getAdminId())
                            .orElseThrow(() -> new MomentException(ErrorCode.ADMIN_NOT_FOUND));
                    return AdminSessionResponse.from(session, admin);
                })
                .collect(Collectors.toList());
    }

    /**
     * 세션 히스토리 조회 (필터링 지원)
     * @param adminId 관리자 ID (null이면 전체)
     * @param startDate 시작일 (null이면 전체)
     * @param endDate 종료일 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 세션 히스토리 페이지
     */
    public Page<AdminSessionHistoryResponse> getSessionHistory(
            Long adminId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Page<AdminSession> sessionPage;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        if (adminId != null && startDateTime != null && endDateTime != null) {
            sessionPage = adminSessionRepository.findSessionHistoryByAdminIdAndDateRange(
                    adminId, startDateTime, endDateTime, pageable);
        } else if (adminId != null) {
            sessionPage = adminSessionRepository.findSessionHistoryByAdminId(adminId, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            sessionPage = adminSessionRepository.findSessionHistoryByDateRange(
                    startDateTime, endDateTime, pageable);
        } else {
            sessionPage = adminSessionRepository.findAllSessionHistory(pageable);
        }

        return sessionPage.map(session -> {
            Admin admin = adminRepository.findById(session.getAdminId())
                    .orElse(null);
            if (admin == null) {
                // 관리자가 삭제된 경우 기본 정보로 생성
                return new AdminSessionHistoryResponse(
                        session.getId(),
                        "삭제된 관리자",
                        "-",
                        session.getLoginTime(),
                        session.getLogoutTime(),
                        session.getIpAddress(),
                        session.getLogoutTime() == null ? "ACTIVE" : "LOGGED_OUT"
                );
            }
            return AdminSessionHistoryResponse.from(session, admin);
        });
    }
}
