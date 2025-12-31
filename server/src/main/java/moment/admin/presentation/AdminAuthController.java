package moment.admin.presentation;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.infrastructure.AdminAuthInterceptor;
import moment.admin.service.admin.AdminService;
import moment.global.exception.MomentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @GetMapping("/admin/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@Valid @ModelAttribute AdminLoginRequest request,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Admin admin = adminService.authenticateAdmin(request.email(), request.password());
            session.setAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY, admin.getId());
            return "redirect:/admin/accounts/new";
        } catch (MomentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
