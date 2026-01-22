package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupJoinRequest(
    @NotBlank(message = "초대 코드는 필수입니다.")
    String inviteCode,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String nickname
) {}
