package moment.admin.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.admin.service.user.AdminUserService;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/admin/users")
    public String listUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model
    ) {
        Page<User> usersPage = adminUserService.getAllUsers(page, size);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("size", size);

        return "admin/users/list";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        User user = adminUserService.getUserById(id);
        AdminUserUpdateRequest request = AdminUserUpdateRequest.from(user);

        model.addAttribute("user", user);
        model.addAttribute("request", request);

        return "admin/users/edit";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String updateUser(
        @PathVariable Long id,
        @Valid @ModelAttribute("request") AdminUserUpdateRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            User user = adminUserService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/users/edit";
        }

        try {
            adminUserService.updateUser(id, request);
            redirectAttributes.addFlashAttribute("message", "사용자 정보가 수정되었습니다.");
            return "redirect:/admin/users";
        } catch (MomentException e) {
            model.addAttribute("error", e.getErrorCode().getMessage());
            User user = adminUserService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/users/edit";
        }
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminUserService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "사용자가 차단되었습니다.");
        } catch (MomentException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/users";
    }
}
