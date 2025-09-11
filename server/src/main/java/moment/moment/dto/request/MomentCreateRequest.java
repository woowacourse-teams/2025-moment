package moment.moment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "모멘트 생성 요청")
public record MomentCreateRequest(
        @Schema(description = "모멘트 내용", example = "오늘 운동 완료!")
        @NotBlank(message = "MOMENT_CONTENT_EMPTY")
        @Size(min = 1, max = 200, message = "MOMENT_INVALID_LENGTH")
        String content,

        @Schema(description = "모멘트 태그 이름 목록", example = "[\"일상/여가\", \"운동\"]")
        @NotNull(message = "TAG_INVALID")
        @Size(min = 1, message = "TAG_INVALID")
        List<@NotBlank(message = "TAG_INVALID") @Pattern(regexp = "^[^\\s]+$", message = "TAG_INVALID") String> tagNames,

        @Schema(
                description = "모멘트 사진 저장 경로",
                example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String imageUrl,

        @Schema(
                description = "모멘트 사진 이름",
                example = "고양이.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String imageName
) {
}
