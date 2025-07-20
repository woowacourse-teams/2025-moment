package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Emoji;

@Schema(description = "내 모멘트의 코멘트의 이모지 조회 응답")
public record MyEmojiResponse1(
        @Schema(description = "이모지 id", example = "1")
        Long id,

        @Schema(description = "이모지를 작성한 유저 id", example = "1")
        Long userId
) {

    public static MyEmojiResponse1 from(Emoji emoji) {
        return new MyEmojiResponse1(emoji.getId(), emoji.getUser().getId());
    }
}
