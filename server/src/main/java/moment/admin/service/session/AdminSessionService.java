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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    // TODO: 세션 수가 증가하면 N+1 쿼리 최적화 필요 (batch loading 또는 join fetch)
    // 현재는 관리자 수가 적어 허용 가능한 수준
    public List<AdminSessionResponse> getAllActiveSessions() {
        List<AdminSession> activeSessions = adminSessionRepository.findAllByLogoutTimeIsNullOrderByLoginTimeDesc();
        return toSessionResponses(activeSessions);
    }

    /**
     * 특정 관리자의 활성 세션 목록 조회
     * @param adminId 관리자 ID
     * @return 세션 목록
     */
    public List<AdminSessionResponse> getActiveSessionsByAdminId(Long adminId) {
        List<AdminSession> activeSessions = adminSessionRepository.findByAdminIdAndLogoutTimeIsNull(adminId);
        return toSessionResponses(activeSessions);
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
        List<AdminSession> activeSessions = findActiveSessionsByFilter(adminId, ipAddress);
        return toSessionResponses(activeSessions);
    }

    private List<AdminSession> findActiveSessionsByFilter(Long adminId, String ipAddress) {
        boolean hasAdminId = adminId != null;
        boolean hasIpAddress = ipAddress != null && !ipAddress.isBlank();

        if (hasAdminId && hasIpAddress) {
            return adminSessionRepository.findByAdminIdAndIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(adminId, ipAddress);
        }
        if (hasAdminId) {
            return adminSessionRepository.findByAdminIdAndLogoutTimeIsNullOrderByLoginTimeDesc(adminId);
        }
        if (hasIpAddress) {
            return adminSessionRepository.findByIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(ipAddress);
        }
        return adminSessionRepository.findAllByLogoutTimeIsNullOrderByLoginTimeDesc();
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

        Page<AdminSession> sessionPage = findSessionHistoryByFilter(adminId, startDate, endDate, pageable);

        // 배치로 Admin 조회 (N+1 해결)
        Set<Long> adminIds = sessionPage.getContent().stream()
                .map(AdminSession::getAdminId)
                .collect(Collectors.toSet());

        Map<Long, Admin> adminMap = adminRepository.findAllByIdIn(adminIds).stream()
                .collect(Collectors.toMap(Admin::getId, Function.identity()));

        return sessionPage.map(session -> toHistoryResponse(session, adminMap.get(session.getAdminId())));
    }

    private Page<AdminSession> findSessionHistoryByFilter(Long adminId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        boolean hasAdminId = adminId != null;
        boolean hasDateRange = startDateTime != null && endDateTime != null;

        if (hasAdminId && hasDateRange) {
            return adminSessionRepository.findSessionHistoryByAdminIdAndDateRange(adminId, startDateTime, endDateTime, pageable);
        }
        if (hasAdminId) {
            return adminSessionRepository.findSessionHistoryByAdminId(adminId, pageable);
        }
        if (hasDateRange) {
            return adminSessionRepository.findSessionHistoryByDateRange(startDateTime, endDateTime, pageable);
        }
        return adminSessionRepository.findAllSessionHistory(pageable);
    }

    private AdminSessionHistoryResponse toHistoryResponse(AdminSession session, Admin admin) {
        if (admin == null) {
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
    }

    /**
     * 세션 목록을 AdminSessionResponse로 변환 (N+1 해결)
     */
    private List<AdminSessionResponse> toSessionResponses(List<AdminSession> sessions) {
        if (sessions.isEmpty()) {
            return List.of();
        }

        Set<Long> adminIds = sessions.stream()
                .map(AdminSession::getAdminId)
                .collect(Collectors.toSet());

        Map<Long, Admin> adminMap = adminRepository.findAllByIdIn(adminIds).stream()
                .collect(Collectors.toMap(Admin::getId, Function.identity()));

        return sessions.stream()
                .map(session -> {
                    Admin admin = adminMap.get(session.getAdminId());
                    if (admin == null) {
                        throw new MomentException(ErrorCode.ADMIN_NOT_FOUND);
                    }
                    return AdminSessionResponse.from(session, admin);
                })
                .collect(Collectors.toList());
    }
}
