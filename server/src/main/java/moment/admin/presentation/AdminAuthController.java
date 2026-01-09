package moment.admin.presentation;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final AdminSessionManager sessionManager;

    @GetMapping("/admin/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@Valid @ModelAttribute AdminLoginRequest request,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Admin admin = adminService.authenticateAdmin(request.email(), request.password());
            sessionManager.setAuth(session, admin.getId(), admin.getRole());
            return "redirect:/admin/users";
        } catch (MomentException e) {
            redirectAttributes.addAttribute("error", e.getErrorCode().getMessage());
            return "redirect:/admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        sessionManager.invalidate(session);
        return "redirect:/admin/login";
    }
}
