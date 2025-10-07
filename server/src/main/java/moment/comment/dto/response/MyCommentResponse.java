package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.dto.tobe.CommentComposition;
import moment.moment.dto.response.tobe.MomentComposition;

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

        @Schema(description = "내 코멘트 알림 정보")
        CommentNotificationResponse commentNotification
) {
    public static MyCommentResponse of(CommentComposition commentComposition,
                                       MomentComposition momentComposition,
                                       List<Long> unreadNotificationIds) {

        List<EchoDetailResponse> echos = commentComposition.echoDetails().stream()
                .map(EchoDetailResponse::from)
                .toList();

        return new MyCommentResponse(
                commentComposition.id(),
                commentComposition.content(),
                commentComposition.imageUrl(),
                commentComposition.commentCreatedAt(),
                MomentDetailResponse.from(momentComposition),
                echos,
                CommentNotificationResponse.from(unreadNotificationIds)
        );
    }
}
