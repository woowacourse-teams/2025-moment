package moment.reply.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;

@Schema(description = "Emoji 조회 응답")
public record EmojisResponse(
        @Schema(description = "조회된 Emoji id")
        Long id,

        @Schema(description = "조회된 Emoji 타입")
        EmojiType emojiType,

        @Schema(description = "조회된 Emoji 작성자")
        String userName
) {

    public static EmojisResponse from(Emoji emoji) {
        return new EmojisResponse(
                emoji.getId(),
                emoji.getEmojiType(),
                emoji.getComment().getCommenter().getNickname()
        );
    }
}
