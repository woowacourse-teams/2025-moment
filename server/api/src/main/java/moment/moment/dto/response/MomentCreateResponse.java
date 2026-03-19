package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;

@Schema(description = "모멘트 응답")
public record MomentCreateResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,

        @Schema(description = "모멘트 작성자", example = "1")
        Long momenterId,

        @Schema(description = "모멘트 작성 시간", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt,

        @Schema(description = "모멘트 내용", example = "야근 힘들어용")
        String content,

        @Schema(description = "모멘트 이미지", example = "1")
        Long imageId
) {

    public static MomentCreateResponse of(Moment moment) {
        return new MomentCreateResponse(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getCreatedAt(),
                moment.getContent(),
                null
        );
    }

    public static MomentCreateResponse of(Moment moment, MomentImage momentImage) {
        return new MomentCreateResponse(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getCreatedAt(),
                moment.getContent(),
                momentImage.getId()
        );
    }
}
