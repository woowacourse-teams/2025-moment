package moment.admin.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.AdminRole;
import moment.admin.global.util.AdminSessionManager;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
            "/admin/accounts/new",
            "/admin/accounts"
    );
    private final AdminSessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        try {
            sessionManager.validateAuthorized(session);
        } catch (MomentException e) {
            response.sendRedirect("/admin/login");
            return false;
        }

        AdminRole role = sessionManager.getRole(session);

        request.setAttribute("adminRole", role);
        request.setAttribute("requestURI", request.getRequestURI());

        String requestURI = request.getRequestURI();
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            response.sendRedirect("/admin/error/forbidden");
            return false;
        }

        return true;
    }

    private boolean isSuperAdminOnlyPath(String requestURI) {
        return SUPER_ADMIN_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
