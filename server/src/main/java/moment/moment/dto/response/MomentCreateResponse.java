package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;

@Schema(description = "모멘트 응답")
public record MomentCreateResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,
        @Schema(description = "모멘트 작성자", example = "1")
        Long momenterId,
        @Schema(description = "모멘트 작성 시간", example = "2025-5-10 10:00")
        LocalDateTime createdAt,
        @Schema(description = "모멘트 내용", example = "야근 힘들어용")
        String content,
        @Schema(description = "모멘트 매칭 여부", example = "false")
        boolean isMatched
) {

    public static MomentCreateResponse of(Moment moment) {
        return new MomentCreateResponse(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getCreatedAt(),
                moment.getContent(),
                moment.isMatched()
        );
    }
}
