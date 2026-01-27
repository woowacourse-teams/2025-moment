package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.moment.domain.Moment;

public record AdminMomentResponse(
    Long momentId,
    String content,
    String imageUrl,
    int commentCount,
    int likeCount,
    AdminMomentAuthorInfo author,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminMomentResponse from(Moment moment, int commentCount) {
        return new AdminMomentResponse(
            moment.getId(),
            moment.getContent(),
            null,
            commentCount,
            0,
            AdminMomentAuthorInfo.from(moment.getMember()),
            moment.getCreatedAt(),
            moment.getDeletedAt()
        );
    }
}
