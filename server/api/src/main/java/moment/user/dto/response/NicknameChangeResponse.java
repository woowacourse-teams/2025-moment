package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.user.domain.User;

@Schema(description = "마이페이지 닉네임 변경 응답")
public record NicknameChangeResponse(
        @Schema(description = "변경된 닉네임", example = "신비로운 하늘의 지구")
        String changedNickname
) {
    public static NicknameChangeResponse from(User user) {
        return new NicknameChangeResponse(user.getNickname());
    }
}
