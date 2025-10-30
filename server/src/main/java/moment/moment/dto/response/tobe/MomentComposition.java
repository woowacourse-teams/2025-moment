package moment.moment.dto.response.tobe;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.user.domain.Level;

public record MomentComposition(
        Long id,
        Long momenterId,
        String content,
        String nickname,
        Level level,
        List<String> tagNames,
        String imageUrl,
        LocalDateTime momentCreatedAt
) {

    public static MomentComposition of(Moment moment,
                                       List<MomentTag> momentTags,
                                       String imageUrl) {

        momentTags = momentTags == null ? Collections.emptyList() : momentTags;

        List<String> tagNames = momentTags.stream()
                .map(MomentTag::getTagName)
                .toList();

        return new MomentComposition(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getContent(),
                moment.getMomenter().getNickname(),
                moment.getMomenter().getLevel(),
                tagNames,
                imageUrl,
                moment.getCreatedAt()
        );
    }
}
