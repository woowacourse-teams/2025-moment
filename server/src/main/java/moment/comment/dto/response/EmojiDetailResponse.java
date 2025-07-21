package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;

@Schema(description = "Comment애 등록된 Emoji 상세 내용")
public record EmojiDetailResponse(
        @Schema(description = "Emoji 아이디", example = "1")
        Long id,

        @Schema(description = "Emoji 타입", example = "HEART")
        EmojiType emojiType,

        @Schema(description = "Emoji를 등록한 유저 아이디", example = "2")
        Long userId
) {
    public static EmojiDetailResponse from(Emoji emoji) {
        return new EmojiDetailResponse(emoji.getId(), emoji.getEmojiType(), emoji.getUser().getId());
    }
}
