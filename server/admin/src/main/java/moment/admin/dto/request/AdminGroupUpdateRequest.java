package moment.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹 정보 수정 요청")
public record AdminGroupUpdateRequest(
    @Schema(description = "그룹명", example = "개발자 모임")
    @NotBlank(message = "그룹명은 필수입니다")
    @Size(max = 30, message = "그룹명은 30자를 초과할 수 없습니다")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    @NotBlank(message = "그룹 설명은 필수입니다")
    @Size(max = 200, message = "그룹 설명은 200자를 초과할 수 없습니다")
    String description
) {}
