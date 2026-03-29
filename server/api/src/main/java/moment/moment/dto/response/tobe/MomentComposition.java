package moment.moment.dto.response.tobe;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public record MomentComposition(
        Long id,
        Long momenterId,
        String content,
        String nickname,
        String originalUrl,
        String optimizedUrl,
        LocalDateTime momentCreatedAt
) {

    public static MomentComposition of(Moment moment, String originalUrl, String optimizedUrl) {
        User momenter = moment.getMomenter();
        return new MomentComposition(
                moment.getId(),
                momenter != null ? momenter.getId() : null,
                moment.getContent(),
                momenter != null ? momenter.getNickname() : "탈퇴한 사용자",
                originalUrl,
                optimizedUrl,
                moment.getCreatedAt()
        );
    }
}
