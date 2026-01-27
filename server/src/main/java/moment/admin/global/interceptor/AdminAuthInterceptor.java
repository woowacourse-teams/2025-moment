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
            "/api/admin/accounts",
            "/api/admin/sessions"
    );
    private final AdminSessionManager sessionManager;
    private final ObjectMapper objectMapper;

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

        // 세션이 없으면 401 응답
        // Spring Session이 자동으로 복원하므로, 여기서 null이면 진짜 세션이 없는 것
        if (session == null) {
            log.warn("No session found");
            return handleUnauthorized(response, "No session found");
        }

        String sessionId = session.getId();

        // 기존 인증 확인 (HTTP 세션 속성)
        try {
            sessionManager.validateAuthorized(session);
            log.debug("Session authorized in HTTP session: sessionId={}", sessionId);
        } catch (MomentException e) {
            log.warn("Admin unauthorized");
            return handleUnauthorized(response, "Unauthorized");
        }

        // DB에서 세션 상태 확인 (차단된 관리자 또는 강제 로그아웃 감지)
        boolean isActiveInDb = sessionManager.isSessionActiveInDb(sessionId);

        if (!isActiveInDb) {
            // DB에서 세션이 비활성화된 경우
            log.warn("Session invalidated in database: sessionId={}", sessionId);
            sessionManager.invalidate(session);  // HTTP 세션도 무효화
            return handleUnauthorized(response, "SESSION_INVALIDATED");
        }

        // 옵션: 마지막 활동 시간 갱신 (5분마다만 갱신하여 성능 최적화)
        // sessionManager.updateLastAccessTime(sessionId);

        AdminRole role = sessionManager.getRole(session);

        // SUPER_ADMIN 전용 경로 체크
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            log.warn("Access denied to SUPER_ADMIN only path: uri={}", requestURI);
            return handleForbidden(response);
        }

        return true;
    }

    private boolean handleUnauthorized(HttpServletResponse response, String reason) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }

    private boolean handleForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }

    private boolean isSuperAdminOnlyPath(String requestURI) {
        return SUPER_ADMIN_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
