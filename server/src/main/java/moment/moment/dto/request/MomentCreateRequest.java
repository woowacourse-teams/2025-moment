package moment.moment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "모멘트 생성 요청")
public record MomentCreateRequest(
        @Schema(description = "모멘트 내용", example = "오늘 운동 완료!")
        @NotBlank(message = "MOMENT_CONTENT_EMPTY")
        @Size(min = 1, max = 200, message = "MOMENT_INVALID_LENGTH")
        String content,

        @Schema(description = "모멘트 태그 이름 목록", example = "[\"일상/여가\", \"운동\"]")
        @NotNull(message = "TAG_NAME_EMPTY")
        List<String> tagNames
) {
}
