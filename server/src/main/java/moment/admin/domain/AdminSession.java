package moment.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE admin_sessions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AdminSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "session_id", nullable = false, unique = true, length = 512)
    private String sessionId;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "last_access_time", nullable = false)
    private LocalDateTime lastAccessTime;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "user_agent", nullable = false, length = 512)
    private String userAgent;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    // 생성자
    public AdminSession(Long adminId, String sessionId, String ipAddress, String userAgent) {
        this.adminId = adminId;
        this.sessionId = sessionId;
        this.loginTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // 비즈니스 로직 메서드

    /**
     * 세션이 활성 상태인지 확인
     * @return logout_time이 NULL이면 true
     */
    public boolean isActive() {
        return this.logoutTime == null;
    }

    /**
     * 세션을 로그아웃 상태로 표시
     */
    public void markLoggedOut() {
        this.logoutTime = LocalDateTime.now();
    }

    /**
     * 마지막 활동 시간 갱신
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 세션이 만료되었는지 확인
     * @param sessionTimeoutSeconds 세션 타임아웃 (초)
     * @return 마지막 활동 시간 + 타임아웃 < 현재 시간이면 true
     */
    public boolean isExpired(long sessionTimeoutSeconds) {
        if (!isActive()) {
            return true;  // 이미 로그아웃된 세션
        }
        LocalDateTime expirationTime = this.lastAccessTime.plusSeconds(sessionTimeoutSeconds);
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
