package moment.moment.dto.response.tobe;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public record MomentComposition(
        Long id,
        Long momenterId,
        String content,
        String nickname,
        String imageUrl,
        LocalDateTime momentCreatedAt
) {

    public static MomentComposition of(Moment moment, String imageUrl) {
        User momenter = moment.getMomenter();
        return new MomentComposition(
                moment.getId(),
                momenter != null ? momenter.getId() : null,
                moment.getContent(),
                momenter != null ? momenter.getNickname() : "탈퇴한 사용자",
                imageUrl,
                moment.getCreatedAt()
        );
    }
}
