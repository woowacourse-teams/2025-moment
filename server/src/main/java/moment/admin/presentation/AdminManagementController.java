package moment.admin.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.service.admin.AdminService;
import moment.global.exception.ErrorCode;
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
    public String newAccountPage(Model model) {
        model.addAttribute("request", new AdminCreateRequest("", "", ""));
        return "admin/accounts/new";
    }

    @PostMapping("/admin/accounts/new")
    public String createAccount(@Valid @ModelAttribute("request") AdminCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력 정보를 확인해주세요.");
            return "admin/accounts/new";
        }

        try {
            adminService.createAdmin(request.email(), request.name(), request.password());
            redirectAttributes.addFlashAttribute("message", "관리자가 성공적으로 등록되었습니다.");
            return "redirect:/admin/accounts";
        } catch (MomentException e) {
            if (e.getErrorCode() == ErrorCode.ADMIN_EMAIL_CONFLICT) {
                model.addAttribute("error", "이미 등록된 이메일입니다.");
            } else {
                model.addAttribute("error", "관리자 등록에 실패했습니다.");
            }
            return "admin/accounts/new";
        }
    }
}
