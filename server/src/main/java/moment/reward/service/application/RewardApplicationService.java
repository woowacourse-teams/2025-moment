package moment.reward.service.application;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.Reason;
import moment.reward.service.reward.RewardService;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardApplicationService {

    private final RewardService rewardService;
    private final UserService userService;

    @Transactional
    public void saveRewardHistory(Reason rewardReason, Long userId) {
        User user = userService.getUserBy(userId);
        rewardService.save(user, rewardReason, userId);
    }

    @Transactional
    public void rewardForComment(Long userId, Reason reason, Long commentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForComment(user, reason, commentId);
    }

    @Transactional
    public void rewardForMoment(Long userId, Reason reason, Long momentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForMoment(user, reason, momentId);
    }

    @Transactional
    public void rewardForEcho(Long userId, Reason reason, Long commentId) {
        User user = userService.getUserBy(userId);
        rewardService.rewardForEcho(user, reason, commentId);
    }

    @Transactional
    public void useReward(Long userId, Reason reason, Long contentId) {
        User user = userService.getUserBy(userId);
        rewardService.useReward(user, reason, contentId);
    }

    public MyRewardHistoryPageResponse getRewardHistoryBy(Long userId, int pageNum, int pageSize) {
        User user = userService.getUserBy(userId);
        return rewardService.getRewardHistoryByUser(user, pageNum, pageSize);
    }
}
