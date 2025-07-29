package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import moment.user.domain.User;

@Schema(description = "회원가입 요청")
public record UserCreateRequest(
        @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
        @NotBlank(message = "EMAIL_INVALID")
        String email,

        @Schema(description = "사용자 비밀번호", example = "1234")
        @Pattern(regexp = "^(?=.[a-z])(?=.\\d).{8,16}$", message = "PASSWORD_INVALID")
        @NotBlank(message = "PASSWORD_INVALID")
        String password,

        @Schema(description = "비밀번호 확인", example = "1234")
        @Pattern(regexp = "^(?=.[a-z])(?=.\\d).{8,16}$", message = "PASSWORD_INVALID")
        @NotBlank(message = "PASSWORD_INVALID")
        String rePassword,

        @Schema(description = "사용자 닉네임", example = "mimi")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,6}$", message = "NICKNAME_INVALID")
        @NotBlank(message = "NICKNAME_INVALID")
        String nickname
) {

    public User toUser() {
        return new User(email, password, nickname);
    }
}
