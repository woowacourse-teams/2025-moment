package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;

@Schema(description = "내 모멘트의 코멘트의 이모지 조회 응답")
public record EmojiDetailResponse(
        @Schema(description = "조회된 Emoji id")
        Long id,

        @Schema(description = "조회된 Emoji 타입")
        EmojiType emojiType,

        @Schema(description = "조회된 Emoji 작성자")
        String userName
) {

    public static EmojiDetailResponse from(Emoji emoji) {
        return new EmojiDetailResponse(emoji.getId(), emoji.getEmojiType(), emoji.getUser().getNickname());
    }
}
