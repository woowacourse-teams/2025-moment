package moment.admin.presentation;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.infrastructure.AdminAuthInterceptor;
import moment.admin.service.admin.AdminService;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminService adminService;

    @GetMapping("/admin/accounts/new")
    public String newAccountPage(HttpSession session, Model model) {
        // 이중 권한 검증 (방어적 프로그래밍)
        Long adminId = (Long) session.getAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY);
        adminService.validateAdminRegistrationPermission(adminId);

        model.addAttribute("request", new AdminCreateRequest("", "", ""));
        return "admin/accounts/new";
    }

    @PostMapping("/admin/accounts/new")
    public String createAccount(@Valid @ModelAttribute("request") AdminCreateRequest request,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        // 이중 권한 검증 (방어적 프로그래밍)
        Long adminId = (Long) session.getAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY);
        adminService.validateAdminRegistrationPermission(adminId);

        if (bindingResult.hasErrors()) {
            return "admin/accounts/new";
        }

        try {
            adminService.createAdmin(request.email(), request.name(), request.password());
            redirectAttributes.addFlashAttribute("message", "관리자가 성공적으로 등록되었습니다.");
            return "redirect:/admin/users";
        } catch (MomentException e) {
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "admin/accounts/new";
        }
    }
}
