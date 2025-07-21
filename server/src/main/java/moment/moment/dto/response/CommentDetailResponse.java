package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record CommentDetailResponse(
        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt,

        List<EmojiDetailResponse> emojis
) {

    public static CommentDetailResponse of(Comment comment, List<Emoji> emojis) {
        List<EmojiDetailResponse> emojiDetailRespons = emojis.stream()
                .map(EmojiDetailResponse::from)
                .toList();
        return new CommentDetailResponse(comment.getContent(), comment.getCreatedAt(), emojiDetailRespons);
    }
}
