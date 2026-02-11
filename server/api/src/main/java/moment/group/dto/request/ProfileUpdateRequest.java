package moment.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(
    @Schema(description = "변경할 닉네임", example = "새닉네임")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String nickname
) {}
