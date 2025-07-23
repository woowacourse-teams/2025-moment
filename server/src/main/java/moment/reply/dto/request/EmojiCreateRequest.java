package moment.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이모지 등록 요청")
public record EmojiCreateRequest(
        @Schema(description = "이모지 타입", example = "HEART")
        String emojiType,

        @Schema(description = "코멘트 id", example = "1")
        Long commentId){
}
