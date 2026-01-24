package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.dto.tobe.CommentComposition;
import moment.moment.dto.response.tobe.MomentComposition;

@Schema(description = "그룹 내 나의 Comment 단일 응답")
public record MyGroupCommentResponse(
        @Schema(description = "등록된 Comment id", example = "1")
        Long id,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 이미지 url", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Comment가 등록된 Moment")
        MyGroupCommentMomentResponse moment,

        @Schema(description = "내 코멘트 알림 정보")
        CommentNotificationResponse commentNotification
) {
    public static MyGroupCommentResponse of(
            CommentComposition commentComposition,
            MomentComposition momentComposition,
            List<Long> unreadNotificationIds
    ) {
        MyGroupCommentMomentResponse momentDetail = momentComposition == null
                ? null
                : MyGroupCommentMomentResponse.from(momentComposition);

        return new MyGroupCommentResponse(
                commentComposition.id(),
                commentComposition.content(),
                commentComposition.imageUrl(),
                commentComposition.commentCreatedAt(),
                momentDetail,
                CommentNotificationResponse.from(unreadNotificationIds)
        );
    }
}
