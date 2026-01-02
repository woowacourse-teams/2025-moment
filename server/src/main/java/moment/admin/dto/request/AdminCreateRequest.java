package moment.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Email(message = "ADMIN_INVALID_INFO")
        String email,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(max = 100, message = "ADMIN_INVALID_INFO")
        String name,

        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(min = 8, message = "ADMIN_INVALID_INFO")
        String password
) {
}
