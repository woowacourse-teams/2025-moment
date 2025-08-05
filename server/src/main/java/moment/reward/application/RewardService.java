package moment.reward.application;

import moment.reward.domain.Reason;
import moment.user.domain.User;

public interface RewardService {

    void reward(User user, Reason reason, Long contentId);
}
