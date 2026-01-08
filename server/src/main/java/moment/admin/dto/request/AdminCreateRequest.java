package moment.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "ADMIN_INVALID_INFO")
        @NotBlank(message = "ADMIN_INVALID_INFO")
        String email,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^.{1,15}$", message = "ADMIN_INVALID_INFO")
        String name,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(message = "ADMIN_INVALID_INFO")
        String password
) {
}
