package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import moment.moment.domain.MomentTag;

@Schema(description = "태그 이름 응답")
public record TagNamesResponse(
        @Schema(description = "태그 이름 리스트", example = "[\"일상/여가\", \"운동\"]")
        List<String> tagNames
) {

    public static TagNamesResponse from(List<MomentTag> tags) {
        return new TagNamesResponse(tags.stream()
                .map(MomentTag::getTagName)
                .toList());
    }
}
