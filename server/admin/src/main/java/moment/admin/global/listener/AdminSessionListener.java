package moment.admin.global.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.AdminSession;
import moment.admin.infrastructure.AdminSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * HTTP 세션 생명주기 이벤트 리스너
 * 세션 종료 시 DB에 로그아웃 시간을 기록합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSessionListener implements HttpSessionListener {

    private final AdminSessionRepository adminSessionRepository;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 세션 생성 시에는 아무 작업도 하지 않음 (로그인 시 별도 처리)
        log.debug("New HTTP session created: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        log.debug("HTTP session destroyed: {}", sessionId);

        // DB에서 세션 조회 후 로그아웃 처리
        Optional<AdminSession> adminSession = adminSessionRepository.findBySessionId(sessionId);

        if (adminSession.isPresent() && adminSession.get().isActive()) {
            AdminSession session = adminSession.get();
            session.markLoggedOut();
            adminSessionRepository.save(session);
            log.info("Admin session marked as logged out: sessionId={}, adminId={}",
                    sessionId, session.getAdminId());
        }
    }
}
