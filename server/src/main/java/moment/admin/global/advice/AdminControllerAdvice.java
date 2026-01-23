package moment.admin.global.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.AdminRole;
import moment.admin.global.util.AdminSessionManager;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Admin 컨트롤러 전역 설정
 * 모든 Admin 페이지에서 공통으로 사용할 Model 속성을 추가합니다.
 */
@Slf4j
@ControllerAdvice(basePackages = "moment.admin.presentation")
@RequiredArgsConstructor
public class AdminControllerAdvice {

    private final AdminSessionManager sessionManager;

    /**
     * 모든 Admin 컨트롤러에 adminRole과 requestURI를 추가
     * 사이드바에서 현재 페이지 강조 및 권한별 메뉴 표시에 사용됩니다.
     */
    @ModelAttribute("adminRole")
    public AdminRole addAdminRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // 세션이 없으면 null 반환 (로그인 전 상태)
        if (session == null) {
            return null;
        }

        // 세션 속성에서 adminRole 직접 가져오기 (예외 발생 방지)
        try {
            Object roleObj = session.getAttribute("ADMIN_ROLE");
            if (roleObj instanceof AdminRole) {
                return (AdminRole) roleObj;
            }
        } catch (Exception e) {
            log.debug("Failed to get admin role from session: {}", e.getMessage());
        }

        return null;
    }

    @ModelAttribute("requestURI")
    public String addRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
