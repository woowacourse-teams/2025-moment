package moment.admin.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpSession;
import moment.admin.domain.Admin;
import moment.admin.dto.response.AdminSessionDetailResponse;
import moment.admin.dto.response.AdminSessionHistoryResponse;
import moment.admin.dto.response.AdminSessionResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.application.AdminManagementApplicationService;
import moment.admin.service.session.AdminSessionService;
import moment.global.exception.MomentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private final AdminSessionManager sessionManager;
    private final AdminService adminService;

    /**
     * 활성 세션 목록 조회 (필터링 지원)
     */
    @GetMapping("/admin/sessions")
    public String listActiveSessions(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String ipAddress,
            Model model) {

        List<AdminSessionResponse> sessions = sessionService.getFilteredActiveSessions(adminId, ipAddress);
        List<Admin> admins = adminService.getAllAdminsWithoutPaging();

        model.addAttribute("sessions", sessions);
        model.addAttribute("totalSessions", sessions.size());
        model.addAttribute("admins", admins);
        model.addAttribute("selectedAdminId", adminId);
        model.addAttribute("ipAddress", ipAddress);

        return "admin/sessions/list";
    }

    /**
     * 세션 상세 정보 조회
     */
    @GetMapping("/admin/sessions/{id}")
    public String sessionDetail(@PathVariable Long id, Model model) {
        AdminSessionDetailResponse adminSession = sessionService.getSessionDetail(id);
        model.addAttribute("adminSession", adminSession);
        return "admin/sessions/detail";
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

    /**
     * 특정 관리자의 모든 세션 강제 로그아웃
     */
    @PostMapping("/admin/sessions/invalidate-by-admin/{adminId}")
    public String invalidateAllSessionsForAdmin(@PathVariable Long adminId,
                                                 HttpSession currentSession,
                                                 RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = sessionManager.getId(currentSession);
            applicationService.forceLogoutAllSessionsForAdmin(currentAdminId, adminId);
            redirectAttributes.addFlashAttribute("message", "해당 관리자의 모든 세션이 강제 로그아웃되었습니다.");
        } catch (MomentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/sessions";
    }

    /**
     * 세션 히스토리 조회
     */
    @GetMapping("/admin/sessions/history")
    public String sessionHistory(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminSessionHistoryResponse> history = sessionService.getSessionHistory(adminId, startDate, endDate, pageable);
        List<Admin> admins = adminService.getAllAdminsWithoutPaging();

        model.addAttribute("history", history);
        model.addAttribute("admins", admins);
        model.addAttribute("selectedAdminId", adminId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/sessions/history";
    }
}
