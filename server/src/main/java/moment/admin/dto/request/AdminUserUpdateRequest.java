package moment.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import moment.user.domain.User;

public record AdminUserUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 15, message = "닉네임은 1-15자 사이여야 합니다.")
    String nickname,

    @NotNull(message = "보유 별조각은 필수입니다.")
    @Min(value = 0, message = "보유 별조각은 0 이상이어야 합니다.")
    Integer availableStar,

    @NotNull(message = "경험치 별조각은 필수입니다.")
    @Min(value = 0, message = "경험치 별조각은 0 이상이어야 합니다.")
    Integer expStar
) {
    public static AdminUserUpdateRequest from(User user) {
        return new AdminUserUpdateRequest(
            user.getNickname(),
            user.getAvailableStar(),
            user.getExpStar()
        );
    }
}