package moment.reply.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;

@Schema(description = "Emoji 조회 응답")
public record EmojiReadResponse(
        @Schema(description = "조회된 Emoji id")
        Long id,

        @Schema(description = "조회된 Emoji 타입")
        String emojiType,

        @Schema(description = "조회된 Emoji 작성자")
        String userName
) {

    public static EmojiReadResponse from(Emoji emoji) {
        return new EmojiReadResponse(
                emoji.getId(),
                emoji.getEmojiType(),
                emoji.getUser().getNickname()
        );
    }
}
