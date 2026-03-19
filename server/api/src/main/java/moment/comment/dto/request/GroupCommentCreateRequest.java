package moment.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "그룹 코멘트 생성 요청")
public record GroupCommentCreateRequest(
    @Schema(description = "코멘트 내용", example = "좋은 모멘트네요!")
    @NotBlank(message = "코멘트 내용은 필수입니다.")
    @Size(max = 200, message = "코멘트 내용은 200자 이하여야 합니다.")
    String content,

    @Schema(
            description = "코멘트 사진 저장 경로",
            example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/sample.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String imageUrl,

    @Schema(
            description = "코멘트 사진 이름",
            example = "sample.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String imageName
) {}
