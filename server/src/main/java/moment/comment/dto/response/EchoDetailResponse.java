package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Echo;

@Schema(description = "Comment애 등록된 Emoji 상세 내용")
public record EchoDetailResponse(
        @Schema(description = "Emoji 아이디", example = "1")
        Long id,

        @Schema(description = "Emoji 타입", example = "HEART")
        String emojiType,

        @Schema(description = "Emoji를 등록한 유저 아이디", example = "2")
        Long userId
) {
    public static EchoDetailResponse from(Echo echo) {
        return new EchoDetailResponse(echo.getId(), echo.getEchoType(), echo.getUser().getId());
    }
}
