package moment.admin.presentation;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.response.AdminResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.application.AdminManagementApplicationService;
import moment.global.exception.MomentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminService adminService;
    private final AdminManagementApplicationService applicationService;
    private final AdminSessionManager sessionManager;

    // ===== 기존 메서드 =====

    @GetMapping("/admin/accounts/new")
    public String newAccountPage(HttpSession session, Model model) {

        Long adminId = sessionManager.getId(session);
        adminService.validateAdminRegistrationPermission(adminId);

        model.addAttribute("request", new AdminCreateRequest("", "", ""));
        return "admin/accounts/new";
    }

    @PostMapping("/admin/accounts/new")
    public String createAccount(@Valid @ModelAttribute("request") AdminCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpSession session) {

        Long adminId = sessionManager.getId(session);
        adminService.validateAdminRegistrationPermission(adminId);

        if (bindingResult.hasErrors()) {
            return "admin/accounts/new";
        }

        try {
            adminService.createAdmin(request.email(), request.name(), request.password());

            redirectAttributes.addFlashAttribute("message", "관리자가 성공적으로 등록되었습니다.");
            return "redirect:/admin/accounts";
        } catch (MomentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/accounts/new";
        }
    }

    // ===== 새로 추가할 메서드 =====

    /**
     * 관리자 목록 조회 (차단된 것 포함)
     */
    @GetMapping("/admin/accounts")
    public String listAdmins(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Admin> adminPage = adminService.getAllAdmins(pageable);

        Page<AdminResponse> adminResponses = adminPage.map(AdminResponse::from);

        model.addAttribute("admins", adminResponses.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", adminPage.getTotalPages());
        model.addAttribute("totalItems", adminPage.getTotalElements());

        return "admin/accounts/list";
    }

    /**
     * 관리자 차단 (Soft Delete + 세션 무효화)
     */
    @PostMapping("/admin/accounts/{id}/block")
    public String blockAdmin(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = sessionManager.getId(session);
            applicationService.blockAdminAndInvalidateSessions(currentAdminId, id);

            redirectAttributes.addFlashAttribute("message", "관리자가 차단되었습니다.");
        } catch (MomentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/accounts";
    }

    /**
     * 관리자 차단 해제
     */
    @PostMapping("/admin/accounts/{id}/unblock")
    public String unblockAdmin(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            applicationService.unblockAdmin(id);
            redirectAttributes.addFlashAttribute("message", "관리자 차단이 해제되었습니다.");
        } catch (MomentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/accounts";
    }
}
