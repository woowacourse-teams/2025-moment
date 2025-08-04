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
    public void reward(final User user, final Reason reason) {
        final int point = reason.getPointTo();
        user.addPoint(point);

        final PointHistory pointHistory = new PointHistory(user, point, reason);
        rewardRepository.save(pointHistory);
    }
}
