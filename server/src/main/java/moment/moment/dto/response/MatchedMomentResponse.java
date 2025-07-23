package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;

@Schema(description = "매칭된 모멘트 응답")
public record MatchedMomentResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,

        @Schema(description = "모멘트 내용", example = "야근 힘들어용")
        String content,

        @Schema(description = "모멘트 작성 시간", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt
) {
    public static MatchedMomentResponse from(Moment moment) {
        return new MatchedMomentResponse(moment.getId(), moment.getContent(), moment.getCreatedAt());
    }

    public static MatchedMomentResponse createEmpty() {
        return new MatchedMomentResponse(null, null, null);
    }
}
