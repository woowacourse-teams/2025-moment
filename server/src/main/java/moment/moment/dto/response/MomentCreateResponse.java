package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import software.amazon.awssdk.services.cloudwatch.endpoints.internal.Value.Str;

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

        @Schema(description = "태그 이름 목록", example = "[\"일상/여가\", \"운동\"]")
        List<String> tagNames,

        @Schema(description = "모멘트 이미지", example = "1")
        Long imageId


) {

    public static MomentCreateResponse of(Moment moment, List<MomentTag> momentTags) {
        List<String> tagNames = momentTags.stream()
                .map(MomentTag::getTagName)
                .toList();

        return new MomentCreateResponse(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getCreatedAt(),
                moment.getContent(),
                tagNames,
                null
        );
    }

    public static MomentCreateResponse of(Moment moment, MomentImage momentImage, List<MomentTag> momentTags) {
        List<String> tagNames = momentTags.stream()
                .map(MomentTag::getTagName)
                .toList();

        return new MomentCreateResponse(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getCreatedAt(),
                moment.getContent(),
                tagNames,
                momentImage.getId()
        );
    }
}
