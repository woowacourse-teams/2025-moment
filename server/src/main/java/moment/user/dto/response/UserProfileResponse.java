package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.Level;
import moment.user.domain.User;

@Schema(description = "유저 프로필 응답")
public record UserProfileResponse(
        @Schema(description = "사용자 닉네임", example = "mimi")
        String nickname,

        @Schema(description = "사용자 경험치", example = "100")
        Integer expStar,

        @Schema(description = "사용자 레벨", example = "METEOR")
        Level level,

        @Schema(description = "다음 레벨 요구 경험치", example = "200")
        Integer nextStepExp

) {

    public static UserProfileResponse from(User user) {
        Level level = user.getLevel();
        return new UserProfileResponse(user.getNickname(), user.getAvailableStar(), level, level.getMaxPoints());
    }
}
