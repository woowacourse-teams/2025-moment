package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.moment.domain.Moment;

import java.time.LocalDateTime;

@Schema(description = "Comment가 등록된 Moment 상세 내용")
public record MomentDetailResponse(
        @Schema(description = "Moment 아이디", example = "1")
        Long id,

        @Schema(description = "Moment 내용", example = "테스트를 겨우 통과했어요!")
        String content,

        @Schema(description = "Moment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static MomentDetailResponse from(Moment moment) {
        return new MomentDetailResponse(moment.getId(), moment.getContent(), moment.getCreatedAt());
    }
}
