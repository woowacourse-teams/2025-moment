package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.User;

@Schema(description = "회원가입 요청")
public record UserCreateRequest(
        @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
        String email,

        @Schema(description = "사용자 비밀번호", example = "1234")
        String password,

        @Schema(description = "비밀번호 확인", example = "1234")
        String rePassword,

        @Schema(description = "사용자 닉네임", example = "mimi")
        String nickname
) {

    public User toUser() {
        return new User(email, password, nickname);
    }
}
