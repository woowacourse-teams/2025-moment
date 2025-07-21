package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record CommentReadResponse(
        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt,

        List<EmojiReadResponse> emojis
) {

    public static CommentReadResponse of(Comment comment, List<Emoji> emojis) {
        List<EmojiReadResponse> emojiReadResponses = emojis.stream()
                .map(EmojiReadResponse::from)
                .toList();
        return new CommentReadResponse(comment.getContent(), comment.getCreatedAt(), emojiReadResponses);
    }
}
