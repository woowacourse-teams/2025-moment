package moment.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "마이페이지 닉네임 변경 요청")
public record NicknameChangeRequest(
        @Schema(description = "새로운 닉네임", example = "신비로운 하늘의 지구")
        @Pattern(regexp = "^.{1,15}$", message = "NICKNAME_INVALID")
        @NotBlank(message = "NICKNAME_INVALID")
        String newNickname
) {
}
