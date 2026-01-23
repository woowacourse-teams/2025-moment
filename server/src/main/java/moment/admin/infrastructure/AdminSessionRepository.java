package moment.admin.infrastructure;

import moment.admin.domain.AdminSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminSessionRepository extends JpaRepository<AdminSession, Long> {

    /**
     * 세션 ID로 세션 조회
     * @param sessionId HTTP 세션 ID
     * @return AdminSession
     */
    Optional<AdminSession> findBySessionId(String sessionId);

    /**
     * 특정 관리자의 활성 세션 목록 조회
     * @param adminId 관리자 ID
     * @return 활성 세션 목록 (logout_time이 NULL)
     */
    List<AdminSession> findByAdminIdAndLogoutTimeIsNull(Long adminId);

    /**
     * 모든 활성 세션 조회 (로그인 시간 기준 내림차순)
     * @return 활성 세션 목록
     */
    List<AdminSession> findAllByLogoutTimeIsNullOrderByLoginTimeDesc();

    /**
     * 만료된 세션 조회 (정리용)
     * @param threshold 기준 시간 (last_access_time < threshold인 세션)
     * @return 만료된 활성 세션 목록
     */
    @Query("SELECT s FROM AdminSession s WHERE s.lastAccessTime < :threshold AND s.logoutTime IS NULL")
    List<AdminSession> findExpiredSessions(@Param("threshold") LocalDateTime threshold);

    /**
     * 세션 ID로 삭제
     * @param sessionId HTTP 세션 ID
     */
    void deleteBySessionId(String sessionId);

    /**
     * 특정 관리자의 활성 세션 조회 (로그인 시간 기준 내림차순)
     * @param adminId 관리자 ID
     * @return 해당 관리자의 활성 세션 목록
     */
    List<AdminSession> findByAdminIdAndLogoutTimeIsNullOrderByLoginTimeDesc(Long adminId);

    /**
     * 특정 IP 주소의 활성 세션 조회 (로그인 시간 기준 내림차순)
     * @param ipAddress IP 주소
     * @return 해당 IP 주소의 활성 세션 목록
     */
    List<AdminSession> findByIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(String ipAddress);

    /**
     * 관리자 ID와 IP 주소로 활성 세션 필터링 (로그인 시간 기준 내림차순)
     * @param adminId 관리자 ID
     * @param ipAddress IP 주소
     * @return 필터링된 활성 세션 목록
     */
    List<AdminSession> findByAdminIdAndIpAddressAndLogoutTimeIsNullOrderByLoginTimeDesc(Long adminId, String ipAddress);

    // ===== 세션 히스토리 조회 (Soft Delete 포함) =====

    /**
     * 모든 세션 히스토리 조회 (로그인 시간 기준 내림차순, 페이징)
     * deleted_at은 포함하지 않고 로그아웃된 세션 포함
     */
    @Query("SELECT s FROM AdminSession s ORDER BY s.loginTime DESC")
    Page<AdminSession> findAllSessionHistory(Pageable pageable);

    /**
     * 특정 관리자의 세션 히스토리 조회
     */
    @Query("SELECT s FROM AdminSession s WHERE s.adminId = :adminId ORDER BY s.loginTime DESC")
    Page<AdminSession> findSessionHistoryByAdminId(@Param("adminId") Long adminId, Pageable pageable);

    /**
     * 기간별 세션 히스토리 조회
     */
    @Query("SELECT s FROM AdminSession s WHERE s.loginTime >= :startDate AND s.loginTime < :endDate ORDER BY s.loginTime DESC")
    Page<AdminSession> findSessionHistoryByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 특정 관리자의 기간별 세션 히스토리 조회
     */
    @Query("SELECT s FROM AdminSession s WHERE s.adminId = :adminId AND s.loginTime >= :startDate AND s.loginTime < :endDate ORDER BY s.loginTime DESC")
    Page<AdminSession> findSessionHistoryByAdminIdAndDateRange(
            @Param("adminId") Long adminId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
