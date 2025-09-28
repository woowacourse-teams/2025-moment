package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "구글 회원가입 요청")
public record GoogleOAuthUserCreateRequest(
        @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
        @NotBlank(message = "EMAIL_INVALID")
        String email,

        @Schema(description = "사용자 비밀번호", example = "null")
        String password,

        @Schema(description = "비밀번호 확인", example = "null")
        String rePassword,

        @Schema(description = "사용자 닉네임", example = "mimi")
        @Pattern(regexp = "^.{1,15}$", message = "NICKNAME_INVALID")
        @NotBlank(message = "NICKNAME_INVALID")
        String nickname,

        @Schema(description = "이메일 구독 설정", example = "false")
        @NotNull(message = "EMAIL_SUBSCRIPTION_INVALID")
        Boolean emailSubscription
) {
}
