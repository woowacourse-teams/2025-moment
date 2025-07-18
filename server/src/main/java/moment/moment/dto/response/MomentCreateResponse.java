package moment.moment.dto.response;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;

public record MomentCreateResponse(Long id,
                                   Long momenterID,
                                   LocalDateTime createdAt,
                                   String content,
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
