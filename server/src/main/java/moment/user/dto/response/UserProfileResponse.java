package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.User;

@Schema(description = "유저 프로필 응답")
public record UserProfileResponse(
        @Schema(description = "사용자 닉네임", example = "mimi")
        String nickname
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getNickname());
    }
}
