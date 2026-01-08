package moment.admin.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import moment.admin.domain.AdminRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    public static final String ADMIN_ROLE_KEY = "ADMIN_ROLE";

    private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
            "/admin/accounts/new",
            "/admin/accounts"
    );

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(ADMIN_SESSION_KEY) == null
                || session.getAttribute(ADMIN_ROLE_KEY) == null) {
            response.sendRedirect("/admin/login");
            return false;
        }

        AdminRole role = (AdminRole) session.getAttribute(ADMIN_ROLE_KEY);

        // request에 역할 정보 저장 (Thymeleaf 템플릿용)
        request.setAttribute("adminRole", role);
        request.setAttribute("requestURI", request.getRequestURI());

        // SUPER_ADMIN 전용 경로 접근 제어
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
