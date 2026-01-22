package moment.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import moment.user.domain.User;

public record AdminUserUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 15, message = "닉네임은 1-15자 사이여야 합니다.")
    String nickname
) {
    public static AdminUserUpdateRequest from(User user) {
        return new AdminUserUpdateRequest(user.getNickname());
    }
}
