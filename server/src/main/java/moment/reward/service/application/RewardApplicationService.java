package moment.reward.service.application;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.Reason;
import moment.reward.service.reward.RewardService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardApplicationService {

    private final RewardService rewardService;
    private final UserService userService;

    public void rewardForComment(Long userId, Reason reason, Long commentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForComment(user, reason, commentId);
    }

    public void rewardForMoment(Long userId, Reason reason, Long momentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForMoment(user, reason, momentId);
    }

    public void rewardForEcho(Long userId, Reason reason, Long commentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForEcho(user, reason, commentId);
    }

    public void useReward(Long userId, Reason reason, Long contentId) {
        User user = userService.getUserBy(userId);
        rewardService.useReward(user, reason, contentId);
    }
}
