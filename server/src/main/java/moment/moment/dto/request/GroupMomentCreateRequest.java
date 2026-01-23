package moment.moment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupMomentCreateRequest(
    @NotBlank(message = "모멘트 내용은 필수입니다.")
    @Size(max = 200, message = "모멘트 내용은 200자 이하여야 합니다.")
    String content
) {}
