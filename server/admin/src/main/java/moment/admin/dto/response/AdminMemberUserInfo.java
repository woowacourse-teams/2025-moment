package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.User;

@Schema(description = "멤버의 사용자 정보")
public record AdminMemberUserInfo(
    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "사용자 이메일", example = "user@example.com")
    String email,

    @Schema(description = "사용자 닉네임", example = "사용자닉네임")
    String nickname
) {
    public static AdminMemberUserInfo from(User user) {
        return new AdminMemberUserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname()
        );
    }
}
