package moment.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹 수정 요청")
public record GroupUpdateRequest(
    @Schema(description = "그룹 이름", example = "수정된 그룹명")
    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다.")
    String name,

    @Schema(description = "그룹 설명", example = "수정된 설명입니다.")
    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다.")
    String description
) {}
