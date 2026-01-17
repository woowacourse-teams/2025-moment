package moment.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "ADMIN_INVALID_INFO")
        String email,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(min = 2, max = 15, message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^.{2,15}$", message = "ADMIN_INVALID_INFO")
        String name,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(min = 8, max = 16, message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "ADMIN_INVALID_INFO")
        String password
) {
}
