package moment.moment.dto.response.tobe;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;

public record MomentComposition(
        Long id,
        Long momenterId,
        String content,
        String nickname,
        String imageUrl,
        LocalDateTime momentCreatedAt
) {

    public static MomentComposition of(Moment moment, String imageUrl) {
        return new MomentComposition(
                moment.getId(),
                moment.getMomenter().getId(),
                moment.getContent(),
                moment.getMomenter().getNickname(),
                imageUrl,
                moment.getCreatedAt()
        );
    }
}
