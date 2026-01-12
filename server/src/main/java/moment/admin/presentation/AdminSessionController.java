package moment.admin.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.service.application.AdminManagementApplicationService;
import moment.admin.service.session.AdminSessionService;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 관리자 세션 관리 컨트롤러
 * SUPER_ADMIN 전용
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminSessionController {

    private final AdminSessionService sessionService;
    private final AdminManagementApplicationService applicationService;

    /**
     * 활성 세션 목록 조회
     */
    @GetMapping("/admin/sessions")
    public String listActiveSessions(Model model) {
        List<AdminSessionResponse> sessions = sessionService.getAllActiveSessions();

        model.addAttribute("sessions", sessions);
        model.addAttribute("totalSessions", sessions.size());

        return "admin/sessions/list";
    }

    /**
     * 특정 세션 강제 로그아웃
     */
    @PostMapping("/admin/sessions/{sessionId}/invalidate")
    public String invalidateSession(@PathVariable String sessionId,
                                     RedirectAttributes redirectAttributes) {
        try {
            applicationService.forceLogoutSession(sessionId);
            redirectAttributes.addFlashAttribute("message", "세션이 강제 로그아웃되었습니다.");
        } catch (MomentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/sessions";
    }
}
