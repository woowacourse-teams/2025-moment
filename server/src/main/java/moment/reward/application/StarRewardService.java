package moment.reward.application;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StarRewardService implements RewardService {
    private final RewardRepository rewardRepository;

    @Override
    @Transactional
    public void rewardForMoment(User user, Reason reason, Long contentId) {
        LocalDate today = LocalDate.now();

        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        if (rewardRepository.existsByUserAndReasonAndToday(user, reason, startOfToday, endOfToday)) {
            return;
        }

        givenStar(user, reason, contentId);
    }

    @Override
    @Transactional
    public void rewardForComment(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        givenStar(user, reason, contentId);
    }

    @Override
    @Transactional
    public void rewardForEcho(User user, Reason reason, Long contentId) {
        if (rewardRepository.existsByUserAndReasonAndContentId(user, reason, contentId)) {
            return;
        }

        givenStar(user, reason, contentId);
    }

    private void givenStar(User user, Reason reason, Long contentId) {
        int star = reason.getPointTo();
        user.addStarAndUpdateLevel(star);

        RewardHistory rewardHistory = new RewardHistory(user, star, reason, contentId);
        rewardRepository.save(rewardHistory);
    }
}
