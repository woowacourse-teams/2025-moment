package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.reply.domain.Emoji;

@Schema(description = "내 모멘트 조회 응답")
public record MyMomentResponse(
        @Schema(description = "내 모멘트 내용", example = "야근 힘들어요 퓨ㅠㅠ")
        String content,

        @Schema(description = "내 모멘트 작성 시간,", example = "2025-07-14T16:24:34Z")
        LocalDateTime createdAt,

        MyCommentResponse1 comment
) {

    public static MyMomentResponse of(Moment moment, Comment comment, List<Emoji> emojis) {
        MyCommentResponse1 myCommentResponse1 = MyCommentResponse1.of(comment, emojis);
        return new MyMomentResponse(moment.getContent(), moment.getCreatedAt(), myCommentResponse1);
    }
}
