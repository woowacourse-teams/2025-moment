package moment.admin.global.util;

import jakarta.servlet.http.HttpSession;
import moment.admin.domain.AdminRole;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminSessionManager {

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_ROLE_KEY = "ADMIN_ROLE";

    @Value("${admin.session.timeout}")
    private int sessionTimeout;

    public void setAuth(HttpSession session, Long adminId, AdminRole role) {
        session.setAttribute(ADMIN_SESSION_KEY, adminId);
        session.setAttribute(ADMIN_ROLE_KEY, role);
        session.setMaxInactiveInterval(sessionTimeout);
    }

    public void validateAuthorized(HttpSession session) {
        if (session == null || session.getAttribute(ADMIN_SESSION_KEY) == null
                || session.getAttribute(ADMIN_ROLE_KEY) == null) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public Long getId(HttpSession session) {
        try {
            Object adminId = session.getAttribute(ADMIN_SESSION_KEY);
            if (adminId == null) {
                throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
            }
            return (Long) adminId;
        } catch (ClassCastException e) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public AdminRole getRole(HttpSession session) {
        try {
            Object role = session.getAttribute(ADMIN_ROLE_KEY);
            if (role == null) {
                throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
            }
            return (AdminRole) role;
        } catch (ClassCastException e) {
            throw new MomentException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
    }

    public boolean isSuperAdmin(HttpSession session) {
        return getRole(session).isSuperAdmin();
    }

    public boolean canManageAdmins(HttpSession session) {
        return isSuperAdmin(session);
    }

    public void invalidate(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}
