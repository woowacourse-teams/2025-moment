package moment.moment.dto.response;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;

public record GroupMomentResponse(
    Long momentId,
    String content,
    String memberNickname,
    Long memberId,
    long likeCount,
    boolean hasLiked,
    long commentCount,
    LocalDateTime createdAt
) {
    public static GroupMomentResponse from(Moment moment, long likeCount, boolean hasLiked, long commentCount) {
        return new GroupMomentResponse(
            moment.getId(),
            moment.getContent(),
            moment.getMember() != null ? moment.getMember().getNickname() : null,
            moment.getMember() != null ? moment.getMember().getId() : null,
            likeCount,
            hasLiked,
            commentCount,
            moment.getCreatedAt()
        );
    }
}
