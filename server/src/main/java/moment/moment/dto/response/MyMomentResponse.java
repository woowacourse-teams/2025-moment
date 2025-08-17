package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.reply.domain.Echo;

@Schema(description = "내 모멘트 조회 응답")
public record MyMomentResponse(
        @Schema(description = "모멘트 id", example = "1")
        Long id,

        @Schema(description = "모멘트 작성자 id", example = "1")
        Long momenterId,

        @Schema(description = "내 모멘트 내용", example = "야근 힘들어요 퓨ㅠㅠ")
        String content,

        @Schema(description = "내 모멘트 작성 시간,", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt,

        List<MyMomentCommentResponse> comments
) {

    public static MyMomentResponse of(Moment moment, List<Comment> comments, Map<Long, List<Echo>> echoMap) {
        if (!comments.isEmpty()) {
            List<MyMomentCommentResponse> myMomentCommentResponses = comments.stream()
                    .map(comment -> MyMomentCommentResponse.of(
                            comment, echoMap.getOrDefault(comment.getId(), List.of())))
                    .toList();

            return new MyMomentResponse(
                    moment.getId(),
                    moment.getMomenterId(),
                    moment.getContent(),
                    moment.getCreatedAt(),
                    myMomentCommentResponses);
        }

        return new MyMomentResponse(
                moment.getId(),
                moment.getMomenterId(),
                moment.getContent(),
                moment.getCreatedAt(),
                Collections.emptyList());
    }
}
