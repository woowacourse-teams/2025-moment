package moment.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 상태 확인 응답")
public record LoginCheckResponse(
        @Schema(description = "로그인 상태 여부", example = "true")
        boolean isLogged) {

    public static LoginCheckResponse createNotLogged() {
        return new LoginCheckResponse(false);
    }
    public static LoginCheckResponse createLogged() {
        return new LoginCheckResponse(true);
    }
}
