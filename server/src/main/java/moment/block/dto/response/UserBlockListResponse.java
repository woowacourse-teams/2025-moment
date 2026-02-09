package moment.block.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.block.domain.UserBlock;

@Schema(description = "차단 목록 응답")
public record UserBlockListResponse(
        @Schema(description = "차단된 사용자 ID", example = "2")
        Long blockedUserId,

        @Schema(description = "차단된 사용자 닉네임", example = "mimi")
        String nickname,

        @Schema(description = "차단 시간", example = "2025-07-14T16:24:34")
        LocalDateTime createdAt
) {
    public static UserBlockListResponse from(UserBlock userBlock) {
        return new UserBlockListResponse(
                userBlock.getBlockedUser().getId(),
                userBlock.getBlockedUser().getNickname(),
                userBlock.getCreatedAt()
        );
    }
}
