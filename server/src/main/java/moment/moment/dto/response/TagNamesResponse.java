package moment.moment.dto.response;

import java.util.List;
import moment.moment.domain.MomentTag;

public record TagNamesResponse(List<String> tagNames) {

    public static TagNamesResponse from(List<MomentTag> tags) {
        return new TagNamesResponse(tags.stream()
                .map(MomentTag::getTagName)
                .toList());
    }
}
