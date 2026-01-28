package moment.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 계정 생성 요청")
public record AdminCreateRequest(
        @Schema(description = "관리자 이메일", example = "newadmin@moment.com")
        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "ADMIN_INVALID_INFO")
        String email,

        @Schema(description = "관리자 이름 (2-15자)", example = "홍길동")
        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(min = 2, max = 15, message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^.{2,15}$", message = "ADMIN_INVALID_INFO")
        String name,

        @Schema(description = "비밀번호 (8-16자, 소문자/숫자/특수문자 포함)", example = "Password123!")
        @NotBlank(message = "ADMIN_INVALID_INFO")
        @Size(min = 8, max = 16, message = "ADMIN_INVALID_INFO")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "ADMIN_INVALID_INFO")
        String password
) {
}
