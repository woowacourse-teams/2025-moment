package moment.reward.service.reward;

import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;

public interface RewardService {

    RewardHistory save(User user, Reason rewardReason, Long userId);

    void rewardForMoment(User user, Reason reason, Long contentId);

    void rewardForComment(User user, Reason reason, Long contentId);

    void rewardForEcho(User user, Reason reason, Long contentId);

    void useReward(User user, Reason reason, Long contentId);

    MyRewardHistoryPageResponse getRewardHistoryByUser(User user, Integer pageNum, Integer pageSize);
}
