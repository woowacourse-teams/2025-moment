package moment.admin.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final AdminSessionManager sessionManager;

    @GetMapping("/admin/login")
    public String loginPage(@RequestParam(required = false) String error,
                            HttpSession session,
                            Model model) {
        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        // (인터셉터에서 세션 복원이 이루어지므로 여기서는 단순 체크만)
        if (session != null) {
            try {
                sessionManager.validateAuthorized(session);
                log.debug("Already logged in user accessing login page, redirecting to main");
                return "redirect:/admin/users";
            } catch (MomentException e) {
                // 세션이 유효하지 않으면 로그인 페이지 표시
                log.debug("No valid session, showing login page");
            }
        }

        // 로그인 페이지 표시
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@Valid @ModelAttribute AdminLoginRequest request,
                        HttpSession session,
                        HttpServletRequest httpRequest,
                        RedirectAttributes redirectAttributes) {
        try {
            // 인증
            Admin admin = adminService.authenticateAdmin(request.email(), request.password());

            // 클라이언트 IP 주소 추출
            String ipAddress = getClientIpAddress(httpRequest);

            // User-Agent 추출
            String userAgent = httpRequest.getHeader("User-Agent");
            if (userAgent == null || userAgent.isBlank()) {
                userAgent = "Unknown";
            }

            // 세션 등록 (DB 저장 포함)
            sessionManager.registerSession(session, admin.getId(), admin.getRole(), ipAddress, userAgent);

            log.info("Admin login successful: email={}, ip={}", admin.getEmail(), ipAddress);

            return "redirect:/admin/users";
        } catch (MomentException e) {
            log.warn("Admin login failed: email={}", request.email());
            redirectAttributes.addAttribute("error", e.getErrorCode().getMessage());
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        Long adminId = sessionManager.getId(session);
        sessionManager.invalidate(session);  // HTTP 세션 무효화 (자동으로 DB 업데이트됨 - Listener)

        log.info("Admin logout successful: adminId={}", adminId);
        return "redirect:/admin/login";
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시/로드밸런서를 거친 경우 X-Forwarded-For 헤더에서 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For는 여러 IP를 포함할 수 있음 (쉼표로 구분)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
