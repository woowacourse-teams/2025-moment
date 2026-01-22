package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다.")
    String name,

    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다.")
    String description,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String ownerNickname
) {}
