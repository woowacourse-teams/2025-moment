package moment.reward.application;

import moment.reward.domain.Reason;
import moment.user.domain.User;

public interface RewardService {

    void rewardForMoment(User user, Reason reason, Long contentId);

    void rewardForComment(User user, Reason reason, Long contentId);

    void rewardForEcho(User user, Reason reason, Long contentId);

    void useReward(User user, Reason reason, Long contentId);
}
