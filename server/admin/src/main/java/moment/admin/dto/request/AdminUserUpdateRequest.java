package moment.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import moment.user.domain.User;

@Schema(description = "사용자 정보 수정 요청")
public record AdminUserUpdateRequest(
    @Schema(description = "사용자 닉네임 (1-15자)", example = "새닉네임")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 15, message = "닉네임은 1-15자 사이여야 합니다.")
    String nickname
) {
    public static AdminUserUpdateRequest from(User user) {
        return new AdminUserUpdateRequest(user.getNickname());
    }
}
