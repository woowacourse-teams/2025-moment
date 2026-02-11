package moment.admin.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.AdminRole;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.admin.global.util.AdminSessionManager;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
            "/api/admin/accounts",
            "/api/admin/sessions"
    );
    private final AdminSessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 요청은 CORS preflight이므로 인증 없이 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();

        // 세션이 없으면 예외
        if (session == null) {
            log.warn("No session found");
            throw new AdminException(AdminErrorCode.UNAUTHORIZED);
        }

        String sessionId = session.getId();

        // 기존 인증 확인
        try {
            sessionManager.validateAuthorized(session);
            log.debug("Session authorized: sessionId={}", sessionId);
        } catch (MomentException e) {
            log.warn("Admin unauthorized");
            throw new AdminException(AdminErrorCode.UNAUTHORIZED);
        }

        // DB에서 세션 상태 확인
        boolean isActiveInDb = sessionManager.isSessionActiveInDb(sessionId);
        if (!isActiveInDb) {
            log.warn("Session invalidated in database: sessionId={}", sessionId);
            sessionManager.invalidate(session);
            throw new AdminException(AdminErrorCode.SESSION_EXPIRED);
        }

        AdminRole role = sessionManager.getRole(session);

        // SUPER_ADMIN 전용 경로 체크
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            log.warn("Access denied to SUPER_ADMIN only path: uri={}", requestURI);
            throw new AdminException(AdminErrorCode.FORBIDDEN);
        }

        return true;
    }

    private boolean isSuperAdminOnlyPath(String requestURI) {
        return SUPER_ADMIN_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
