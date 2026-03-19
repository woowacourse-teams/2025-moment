package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "Admin API 성공 응답 DTO")
public record AdminSuccessResponse<T>(
    @Schema(description = "상태 코드", example = "200")
    int status,
    @Schema(description = "응답 데이터")
    T data
) {
    public static <T> AdminSuccessResponse<T> of(HttpStatus httpStatus, T data) {
        return new AdminSuccessResponse<>(httpStatus.value(), data);
    }
}
