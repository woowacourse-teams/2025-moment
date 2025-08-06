package moment.reward.application;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.PointHistory;
import moment.reward.domain.Reason;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointRewardService implements RewardService {

    private final RewardRepository rewardRepository;

    @Override
    @Transactional
    public void reward(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        int point = reason.getPointTo();
        user.addPointAndUpdateLevel(point);

        PointHistory pointHistory = new PointHistory(user, point, reason, contentId);
        rewardRepository.save(pointHistory);
    }
}
