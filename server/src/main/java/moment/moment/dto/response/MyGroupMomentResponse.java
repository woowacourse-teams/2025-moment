package moment.moment.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import moment.moment.domain.Moment;

public record MyGroupMomentResponse(
        Long momentId,
        String content,
        String memberNickname,
        Long memberId,
        long likeCount,
        boolean hasLiked,
        long commentCount,
        LocalDateTime createdAt,
        List<MyGroupMomentCommentResponse> comments,
        MomentNotificationResponse momentNotification
) {
    public static MyGroupMomentResponse of(
            Moment moment,
            long likeCount,
            boolean hasLiked,
            long commentCount,
            List<MyGroupMomentCommentResponse> comments,
            MomentNotificationResponse momentNotification
    ) {
        return new MyGroupMomentResponse(
                moment.getId(),
                moment.getContent(),
                moment.getMember() != null ? moment.getMember().getNickname() : null,
                moment.getMember() != null ? moment.getMember().getId() : null,
                likeCount,
                hasLiked,
                commentCount,
                moment.getCreatedAt(),
                comments,
                momentNotification
        );
    }
}
