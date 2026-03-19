package moment.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹 가입 요청")
public record GroupJoinRequest(
    @Schema(description = "초대 코드", example = "abc123xyz")
    @NotBlank(message = "초대 코드는 필수입니다.")
    String inviteCode,

    @Schema(description = "가입 닉네임", example = "새멤버")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String nickname
) {}
