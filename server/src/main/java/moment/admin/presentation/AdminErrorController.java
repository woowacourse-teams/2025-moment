package moment.admin.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminErrorController {

    @GetMapping("/admin/error/forbidden")
    public String forbiddenPage() {
        return "admin/error/forbidden";
    }
}
