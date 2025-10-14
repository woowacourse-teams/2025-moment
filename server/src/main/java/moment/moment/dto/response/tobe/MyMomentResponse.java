package moment.moment.dto.response.tobe;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.dto.tobe.CommentComposition;
import moment.moment.dto.response.MomentNotificationResponse;
import moment.moment.dto.response.MyMomentCommentResponse;
import moment.moment.dto.response.TagNamesResponse;

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

    public static MyMomentResponse of(MomentComposition momentComposition,
                                      List<CommentComposition> commentCompositions,
                                      List<Long> unreadNotificationIds) {

        List<MyMomentCommentResponse> myMomentCommentResponses = commentCompositions.stream()
                .map(MyMomentCommentResponse::from)
                .toList();

        return new MyMomentResponse(
                momentComposition.id(),
                momentComposition.momenterId(),
                momentComposition.content(),
                new TagNamesResponse(momentComposition.tagNames()),
                momentComposition.imageUrl(),
                momentComposition.momentCreatedAt(),
                myMomentCommentResponses,
                MomentNotificationResponse.from(unreadNotificationIds)
        );
    }
}
