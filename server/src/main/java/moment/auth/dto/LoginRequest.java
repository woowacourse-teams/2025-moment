package moment.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
        String email,

        @Schema(description = "사용자 비밀번호", example = "1234")
        String password
) {
}
