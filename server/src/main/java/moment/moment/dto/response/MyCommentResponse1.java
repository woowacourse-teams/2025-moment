package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record MyCommentResponse1(
        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt,
        
        List<MyEmojiResponse1> emojis
) {

    public static MyCommentResponse1 of(Comment comment, List<Emoji> emojis) {
        List<MyEmojiResponse1> myEmojiResponse1s = emojis.stream()
                .map(MyEmojiResponse1::from)
                .toList();
        return new MyCommentResponse1(comment.getContent(), comment.getCreatedAt(), myEmojiResponse1s);
    }
}
