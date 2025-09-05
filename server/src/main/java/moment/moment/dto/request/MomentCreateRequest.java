package moment.moment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "모멘트 생성 요청")
public record MomentCreateRequest(
        @Schema(description = "모멘트 내용", example = "오늘 운동 완료!")
        @NotBlank(message = "MOMENT_CONTENT_EMPTY")
        @Size(min = 1, max = 200, message = "MOMENT_INVALID_LENGTH")
        String content,

        @Schema(description = "모멘트 태그 이름", example = "일상/여가")
        @NotBlank(message = "TAG_NAME_EMPTY")
        @Size(min = 1, max = 30, message = "TAG_NAME_INVALID_LENGTH")
        String tagName
) {
}
