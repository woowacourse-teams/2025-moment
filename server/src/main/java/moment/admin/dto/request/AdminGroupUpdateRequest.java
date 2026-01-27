package moment.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminGroupUpdateRequest(
    @NotBlank(message = "그룹명은 필수입니다")
    @Size(max = 30, message = "그룹명은 30자를 초과할 수 없습니다")
    String name,

    @NotBlank(message = "그룹 설명은 필수입니다")
    @Size(max = 200, message = "그룹 설명은 200자를 초과할 수 없습니다")
    String description
) {}
