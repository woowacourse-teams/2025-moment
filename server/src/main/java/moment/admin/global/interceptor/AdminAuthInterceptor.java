package moment.admin.global.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.AdminRole;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.util.AdminSessionManager;
import moment.global.exception.MomentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
            "/admin/accounts/new",
            "/admin/accounts",
            "/admin/sessions",
            "/admin/sessions/history",
            "/api/admin/accounts",
            "/api/admin/sessions"
    );
    private final AdminSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String requestURI = request.getRequestURI();

        // 세션이 없으면 로그인 페이지로 리다이렉트
        // Spring Session이 자동으로 복원하므로, 여기서 null이면 진짜 세션이 없는 것
        if (session == null) {
            log.warn("No session found");
            return handleUnauthorized(request, response, "No session found");
        }

        String sessionId = session.getId();

        // 기존 인증 확인 (HTTP 세션 속성)
        try {
            sessionManager.validateAuthorized(session);
            log.debug("Session authorized in HTTP session: sessionId={}", sessionId);
        } catch (MomentException e) {
            log.warn("Admin unauthorized");
            return handleUnauthorized(request, response, "Unauthorized");
        }

        // DB에서 세션 상태 확인 (차단된 관리자 또는 강제 로그아웃 감지)
        boolean isActiveInDb = sessionManager.isSessionActiveInDb(sessionId);

        if (!isActiveInDb) {
            // DB에서 세션이 비활성화된 경우
            log.warn("Session invalidated in database: sessionId={}", sessionId);
            sessionManager.invalidate(session);  // HTTP 세션도 무효화
            return handleUnauthorized(request, response, "SESSION_INVALIDATED");
        }

        // 옵션: 마지막 활동 시간 갱신 (5분마다만 갱신하여 성능 최적화)
        // sessionManager.updateLastAccessTime(sessionId);

        AdminRole role = sessionManager.getRole(session);

        // SUPER_ADMIN 전용 경로 체크
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            log.warn("Access denied to SUPER_ADMIN only path: uri={}", requestURI);
            return handleForbidden(request, response);
        }

        // Note: adminRole과 requestURI는 AdminControllerAdvice에서 Model에 추가됩니다.

        return true;
    }

    private boolean handleUnauthorized(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String reason) throws Exception {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/admin/")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }

        // SSR 요청은 리다이렉트
        response.sendRedirect("/admin/login?error=" + reason);
        return false;
    }

    private boolean handleForbidden(HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/api/admin/")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }

        response.sendRedirect("/admin/error/forbidden");
        return false;
    }

    private boolean isSuperAdminOnlyPath(String requestURI) {
        return SUPER_ADMIN_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
