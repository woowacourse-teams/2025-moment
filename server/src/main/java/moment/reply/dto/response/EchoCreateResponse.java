package moment.reply.dto.response;

import java.time.LocalDateTime;
import moment.reply.domain.Echo;

public record EchoCreateResponse(String emojiType, Long commentId, Long userId, LocalDateTime createdAt) {

    public static EchoCreateResponse from(Echo echo) {
        return new EchoCreateResponse(
                echo.getEchoType(),
                echo.getComment().getId(),
                echo.getUser().getId(),
                echo.getCreatedAt()
        );
    }
}
