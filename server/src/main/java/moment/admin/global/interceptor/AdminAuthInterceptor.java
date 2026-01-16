package moment.admin.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.AdminRole;
import moment.admin.global.util.AdminSessionManager;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
            "/admin/accounts/new",
            "/admin/accounts",
            "/admin/sessions"
    );
    private final AdminSessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        // 세션이 없으면 로그인 페이지로 리다이렉트
        // Spring Session이 자동으로 복원하므로, 여기서 null이면 진짜 세션이 없는 것
        if (session == null) {
            log.warn("No session found, redirecting to login");
            response.sendRedirect("/admin/login");
            return false;
        }

        String sessionId = session.getId();

        // 기존 인증 확인 (HTTP 세션 속성)
        try {
            sessionManager.validateAuthorized(session);
            log.debug("Session authorized in HTTP session: sessionId={}", sessionId);
        } catch (MomentException e) {
            log.warn("Admin unauthorized, redirecting to login");
            response.sendRedirect("/admin/login");
            return false;
        }

        // DB에서 세션 상태 확인 (차단된 관리자 또는 강제 로그아웃 감지)
        boolean isActiveInDb = sessionManager.isSessionActiveInDb(sessionId);

        if (!isActiveInDb) {
            // DB에서 세션이 비활성화된 경우
            log.warn("Session invalidated in database: sessionId={}", sessionId);
            sessionManager.invalidate(session);  // HTTP 세션도 무효화
            response.sendRedirect("/admin/login?error=SESSION_INVALIDATED");
            return false;
        }

        // 옵션: 마지막 활동 시간 갱신 (5분마다만 갱신하여 성능 최적화)
        // sessionManager.updateLastAccessTime(sessionId);

        AdminRole role = sessionManager.getRole(session);

        // SUPER_ADMIN 전용 경로 체크
        String requestURI = request.getRequestURI();
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            log.warn("Access denied to SUPER_ADMIN only path: uri={}", requestURI);
            response.sendRedirect("/admin/error/forbidden");
            return false;
        }

        // Note: adminRole과 requestURI는 AdminControllerAdvice에서 Model에 추가됩니다.

        return true;
    }

    private boolean isSuperAdminOnlyPath(String requestURI) {
        return SUPER_ADMIN_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
