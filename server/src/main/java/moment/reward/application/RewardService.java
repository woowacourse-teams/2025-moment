package moment.reward.application;

import moment.user.domain.User;

public interface RewardService {

    void rewardForCommentCreation(User commenter);

    void rewardForPositiveEmoji(User commenter);
}
