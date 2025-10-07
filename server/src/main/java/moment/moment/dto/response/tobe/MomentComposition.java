package moment.moment.dto.response.tobe;

import java.time.LocalDateTime;
import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;

public record MomentComposition(
        Long id,
        Long momenterId,
        String content,
        List<String> tagNames,
        String imageUrl,
        LocalDateTime momentCreatedAt
) {

    public static MomentComposition of (Moment moment,
                                        List<MomentTag> momentTags,
                                        MomentImage momentImage) {

        List<String> tagNames = momentTags.stream()
                .map(MomentTag::getTagName)
                .toList();

        return new MomentComposition(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getContent(),
                tagNames,
                momentImage.getImageUrl(),
                moment.getCreatedAt()
        );
    }
}
