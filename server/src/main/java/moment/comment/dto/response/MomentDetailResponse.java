package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;

@Schema(description = "Comment가 등록된 Moment 상세 내용")
public record MomentDetailResponse(
        @Schema(description = "Moment 내용", example = "테스트를 겨우 통과했어요!")
        String content,

        @Schema(description = "Moment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static MomentDetailResponse from(Moment moment) {
        return new MomentDetailResponse(moment.getContent(), moment.getCreatedAt());
    }
}
