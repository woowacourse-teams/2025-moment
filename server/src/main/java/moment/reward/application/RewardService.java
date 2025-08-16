package moment.reward.application;

import moment.reward.domain.Reason;
import moment.user.domain.User;

public interface RewardService {
    // 메서드 임시 작성, 나중에 리워드 작업물과 통합될 때 실제 메서드 시그니처들이 들어올 예정
    void rewardForMoment(User user, Reason reason, Long contentId);

    void rewardForComment(User user, Reason reason, Long contentId);

    void rewardForEmoji(User user, Reason reason, Long contentId);
}
