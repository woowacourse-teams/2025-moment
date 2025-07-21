package moment.reply.dto.response;

import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;

public record EmojisResponse(
        EmojiType emojiType,
        String userName
) {

    public static EmojisResponse from(Emoji emoji) {
        return new EmojisResponse(
                emoji.getEmojiType(),
                emoji.getComment().getCommenter().getNickname()
        );
    }
}
