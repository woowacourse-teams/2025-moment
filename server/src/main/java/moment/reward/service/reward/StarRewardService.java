package moment.reward.service.reward;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.User;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.MyRewardHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StarRewardService implements RewardService {

    private final RewardRepository rewardRepository;
    private final Clock clock;

    @Override
    @Transactional
    public RewardHistory save(User user, Reason rewardReason, Long contentId) {
        RewardHistory rewardHistory = new RewardHistory(user, rewardReason, contentId);
        return rewardRepository.save(rewardHistory);
    }

    @Override
    @Transactional
    public void rewardForMoment(User momenter, Reason reason, Long momentId) {
        LocalDate today = LocalDate.now(clock);

        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        if (stopIfDuplicateMomentRewardFound(momenter, reason, startOfToday, endOfToday)) {
            return;
        }

        updateStar(momenter, reason, momentId);
    }

    private boolean stopIfDuplicateMomentRewardFound(User momenter, Reason reason, LocalDateTime startOfToday,
                                                     LocalDateTime endOfToday) {
        return rewardRepository.existsByUserAndReasonAndToday(momenter, reason, startOfToday, endOfToday);
    }

    @Override
    @Transactional
    public void rewardForComment(User commenter, Reason reason, Long commentId) {
        if (stopIfDuplicateCommentRewardFound(commenter, reason, commentId)) {
            return;
        }

        updateStar(commenter, reason, commentId);
    }

    private boolean stopIfDuplicateCommentRewardFound(User commenter, Reason reason, Long commentId) {
        return rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, commentId);
    }

    @Override
    @Transactional
    public void rewardForEcho(User commenter, Reason reason, Long echoId) {
        if (stopIfDuplicateEchoRewardFound(commenter, reason, echoId)) {
            return;
        }

        updateStar(commenter, reason, echoId);
    }

    private boolean stopIfDuplicateEchoRewardFound(User commenter, Reason reason, Long echoId) {
        return rewardRepository.existsByUserAndReasonAndContentId(commenter, reason, echoId);
    }

    @Override
    public void useReward(User user, Reason reason, Long contentId) {
        if (user.canNotUseStars(reason.getPointTo())) {
            throw new MomentException(ErrorCode.USER_NOT_ENOUGH_STAR);
        }
        updateStar(user, reason, contentId);
    }

    private void updateStar(User user, Reason reason, Long contentId) {
        int star = reason.getPointTo();
        user.addStarAndUpdateLevel(star);

        RewardHistory rewardHistory = new RewardHistory(user, reason, contentId);
        rewardRepository.save(rewardHistory);
    }

    @Override
    public MyRewardHistoryPageResponse getRewardHistoryByUser(User user, Integer pageNum, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<RewardHistory> rewardHistoryPage = rewardRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        Page<MyRewardHistoryResponse> page = rewardHistoryPage.map(MyRewardHistoryResponse::from);

        return MyRewardHistoryPageResponse.from(page);
    }
}
