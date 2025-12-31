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

        return true;
    }
}
