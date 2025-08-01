package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;

@Schema(description = "내 모멘트의 코멘트의 이모지 조회 응답")
public record MyMomentEmojiResponse(
        @Schema(description = "조회된 Emoji id")
        Long id,

        @Schema(description = "조회된 Emoji 타입")
        String emojiType,

        @Schema(description = "조회된 Emoji 작성자")
        String userName
) {

    public static MyMomentEmojiResponse from(Emoji emoji) {
        return new MyMomentEmojiResponse(emoji.getId(), emoji.getEmojiType(), emoji.getUser().getNickname());
    }
}
