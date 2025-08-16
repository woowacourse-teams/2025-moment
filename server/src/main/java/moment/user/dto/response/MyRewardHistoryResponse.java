package moment.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 나의 보상 기록 상세 응답")
public record MyRewardHistoryResponse(
        @Schema(description = "나의 보상 기록 id", example = "1")
        Long id,

        @Schema(description = "별조각 변화량", example = "5")
        Integer changeStar,

        @Schema(description = "보상 사유", example = "MOMENT_CREATION")
        Reason reason,

        @Schema(description = "보상 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static MyRewardHistoryResponse from(RewardHistory rewardHistory) {
        return new MyRewardHistoryResponse(
                rewardHistory.getId(),
                rewardHistory.getAmount(),
                rewardHistory.getReason(),
                rewardHistory.getCreatedAt()
        );
    }
}
