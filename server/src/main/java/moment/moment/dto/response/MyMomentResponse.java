package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.notification.domain.Notification;
import moment.reply.domain.Echo;

@Schema(description = "내 모멘트 조회 응답")
public record MyMomentResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,

        @Schema(description = "모멘트 작성자 id", example = "1")
        Long momenterId,

        @Schema(description = "내 모멘트 내용", example = "야근 힘들어요 퓨ㅠㅠ")
        String content,

        @Schema(description = "태그 이름", example = "일상/여가")
        TagNamesResponse tagNames,

        @Schema(description = "모멘트 이미지", example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "내 모멘트 작성 시간,", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt,

        List<MyMomentCommentResponse> comments,

        @Schema(description = "내 모멘트 알림 정보")
        MomentNotificationResponse momentNotification
) {

    public static MyMomentResponse of(
            Moment moment,
            List<Comment> comments,
            Map<Long, List<Echo>> echoMap,
            List<MomentTag> momentTags,
            MomentImage momentImage,
            Map<Comment, CommentImage> commentImages,
            List<Notification> unreadNotifications
    ) {
        String imageUrl = Optional.ofNullable(momentImage)
                .map(MomentImage::getImageUrl)
                .orElse(null);

        if (!comments.isEmpty()) {
            List<MyMomentCommentResponse> myMomentCommentResponses = comments.stream()
                    .map(comment -> MyMomentCommentResponse.of(
                            comment, echoMap.getOrDefault(comment.getId(), List.of()),
                            commentImages.getOrDefault(comment, null)))
                    .toList();

            return new MyMomentResponse(
                    moment.getId(),
                    moment.getMomenterId(),
                    moment.getContent(),
                    TagNamesResponse.from(momentTags),
                    imageUrl,
                    moment.getCreatedAt(),
                    myMomentCommentResponses,
                    MomentNotificationResponse.from(unreadNotifications)
            );
        }

        return new MyMomentResponse(
                moment.getId(),
                moment.getMomenterId(),
                moment.getContent(),
                TagNamesResponse.from(momentTags),
                imageUrl,
                moment.getCreatedAt(),
                Collections.emptyList(),
                MomentNotificationResponse.from(unreadNotifications)
        );
    }
}
