package moment.admin.infrastructure;

import moment.admin.domain.AdminSession;
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
}
