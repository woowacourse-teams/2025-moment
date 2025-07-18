package moment.moment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모멘트 생성 요청")
public record MomentCreateRequest(
        @Schema(description = "모멘트 내용", example = "오늘 운동 완료!")
        String content
) {
}
