package moment.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹 생성 요청")
public record GroupCreateRequest(
    @Schema(description = "그룹 이름", example = "개발자 모임")
    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다.")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다.")
    String description,

    @Schema(description = "소유자 닉네임", example = "홍길동")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String ownerNickname
) {}
