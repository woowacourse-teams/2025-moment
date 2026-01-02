package moment.admin.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_SESSION_KEY = "ADMIN_ID";

    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(ADMIN_SESSION_KEY) == null) {
            response.sendRedirect("/admin/login");
            return false;
        }

        // Thymeleaf 템플릿에서 현재 URL을 확인하기 위해 추가
        request.setAttribute("requestURI", request.getRequestURI());

        return true;
    }
}
