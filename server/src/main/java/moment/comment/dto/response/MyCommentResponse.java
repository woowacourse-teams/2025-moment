package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.notification.domain.Notification;
import moment.reply.domain.Echo;

@Schema(description = "나의 Comment 목록 조회 응답")
public record MyCommentResponse(
        @Schema(description = "등록된 Comment id", example = "1")
        Long id,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 이미지 url", example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Comment가 등록된 Moment")
        MomentDetailResponse moment,

        @Schema(description = "Comment에 등록된 에코 목록")
        List<EchoDetailResponse> echos,

        CommentNotificationResponse commentNotificationResponse
) {
    public static MyCommentResponse from(
            Comment comment,
            List<MomentTag> momentTags,
            CommentImage commentImage,
            MomentImage momentImage,
            List<Notification> notifications
    ) {
        String imageUrl = Optional.ofNullable(commentImage)
                .map(CommentImage::getImageUrl)
                .orElse(null);

        List<EchoDetailResponse> echosResponse = null;
        Moment momentOfComment = comment.getMoment();

        if (momentOfComment == null) {
            return new MyCommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    imageUrl,
                    comment.getCreatedAt(),
                    null,
                    echosResponse,
                    CommentNotificationResponse.of(notifications)
            );
        }

        MomentDetailResponse momentResponse = MomentDetailResponse.from(momentOfComment, momentTags, momentImage);

        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                imageUrl,
                comment.getCreatedAt(),
                momentResponse,
                echosResponse,
                CommentNotificationResponse.of(notifications)
        );
    }

    public static MyCommentResponse from(
            Comment comment,
            List<Echo> echoes,
            List<MomentTag> momentTags,
            CommentImage commentImage,
            MomentImage momentImage,
            List<Notification> notifications
    ) {
        String imageUrl = Optional.ofNullable(commentImage)
                .map(CommentImage::getImageUrl)
                .orElse(null);
        List<EchoDetailResponse> echosResponse = echoes.stream()
                .map(EchoDetailResponse::from)
                .toList();

        Moment momentOfComment = comment.getMoment();

        if (momentOfComment == null) {
            return new MyCommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    imageUrl,
                    comment.getCreatedAt(),
                    null,
                    echosResponse,
                    CommentNotificationResponse.of(notifications)
            );
        }

        MomentDetailResponse momentResponse = MomentDetailResponse.from(momentOfComment, momentTags, momentImage);
        return new MyCommentResponse(
                comment.getId(),
                comment.getContent(),
                imageUrl,
                comment.getCreatedAt(),
                momentResponse,
                echosResponse,
                CommentNotificationResponse.of(notifications)
        );
    }
}
