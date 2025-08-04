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
public class PointRewardService implements RewardService{

    private final RewardRepository rewardRepository;

    @Transactional
    @Override
    public void rewardForCommentCreation(User commenter) {
        reward(commenter, Reason.COMMENT_CREATION);
    }

    @Transactional
    @Override
    public void rewardForPositiveEmoji(User commenter) {
        reward(commenter, Reason.POSITIVE_EMOJI_RECEIVED);
    }

    private void reward(User user, Reason reason) {
        int point = reason.getPointTo();

        user.addPoint(point);

        PointHistory pointHistory = new PointHistory(user, point, reason);
        rewardRepository.save(pointHistory);
    }
}
