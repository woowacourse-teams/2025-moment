package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Echo;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record MyMomentCommentResponse(
        @Schema(description = "코멘트 id", example = "1")
        Long id,

        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt,

        List<MyMomentEchoResponse> emojis
) {

    public static MyMomentCommentResponse of(Comment comment, List<Echo> echoes) {
        List<MyMomentEchoResponse> emojiDetailRespons = echoes.stream()
                .map(MyMomentEchoResponse::from)
                .toList();
        
        return new MyMomentCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                emojiDetailRespons);
    }
}
