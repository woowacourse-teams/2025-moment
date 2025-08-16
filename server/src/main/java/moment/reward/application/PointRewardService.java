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

    // 메서드 임시 작성, 나중에 리워드 작업물과 통합될 때 실제 로직이 들어올 예정
    @Override
    @Transactional
    public void rewardForMoment(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        int point = reason.getPointTo();
        user.addPointAndUpdateLevel(point);

        PointHistory pointHistory = new PointHistory(user, point, reason, contentId);
        rewardRepository.save(pointHistory);
    }

    @Override
    @Transactional
    public void rewardForComment(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        int point = reason.getPointTo();
        user.addPointAndUpdateLevel(point);

        PointHistory pointHistory = new PointHistory(user, point, reason, contentId);
        rewardRepository.save(pointHistory);
    }

    @Override
    @Transactional
    public void rewardForEmoji(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        int point = reason.getPointTo();
        user.addPointAndUpdateLevel(point);

        PointHistory pointHistory = new PointHistory(user, point, reason, contentId);
        rewardRepository.save(pointHistory);
    }
}
