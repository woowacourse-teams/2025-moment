package moment.reply.dto.response;

import java.time.LocalDateTime;
import moment.reply.domain.Emoji;

public record EmojiCreateResponse(String emojiType, Long commentId, Long userId, LocalDateTime createdAt) {

    public static EmojiCreateResponse from(Emoji emoji) {
        return new EmojiCreateResponse(
                emoji.getEmojiType(),
                emoji.getComment().getId(),
                emoji.getUser().getId(),
                emoji.getCreatedAt()
        );
    }
}
