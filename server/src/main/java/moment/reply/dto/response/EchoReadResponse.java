package moment.reply.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reply.domain.Echo;

@Schema(description = "Emoji 조회 응답")
public record EchoReadResponse(
        @Schema(description = "조회된 Emoji id")
        Long id,

        @Schema(description = "조회된 Emoji 타입")
        String emojiType,

        @Schema(description = "조회된 Emoji 작성자")
        String userName
) {

    public static EchoReadResponse from(Echo echo) {
        return new EchoReadResponse(
                echo.getId(),
                echo.getEchoType(),
                echo.getUser().getNickname()
        );
    }
}
