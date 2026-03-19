package moment.block.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.block.domain.UserBlock;

@Schema(description = "사용자 차단 응답")
public record UserBlockResponse(
        @Schema(description = "차단된 사용자 ID", example = "2")
        Long blockedUserId,

        @Schema(description = "차단 시간", example = "2025-07-14T16:24:34")
        LocalDateTime createdAt
) {
    public static UserBlockResponse from(UserBlock userBlock) {
        return new UserBlockResponse(
                userBlock.getBlockedUser().getId(),
                userBlock.getCreatedAt()
        );
    }
}
