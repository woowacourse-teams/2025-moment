package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.domain.User;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.NicknameChangeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserQueryService userQueryService;

    private final RewardService rewardService;

    public MyPageProfileResponse getProfile(Long userId) {
        User user = userQueryService.getUserById(userId);
        return MyPageProfileResponse.from(user);
    }

    public MyRewardHistoryPageResponse getMyRewardHistory(Integer pageNum, Integer pageSize, Long userId) {

        User user = userQueryService.getUserById(userId);

        return rewardService.getRewardHistoryByUser(user, pageNum, pageSize);
    }

    @Transactional
    public NicknameChangeResponse changeNickname(NicknameChangeRequest request, Long userId) {
        User user = userQueryService.getUserById(userId);

        Reason rewardReason = Reason.NICKNAME_CHANGE;
        int requiredStar = rewardReason.getPointTo();

        validateEnoughStars(user, requiredStar);
        validateNicknameConflict(request);

        user.updateNickname(request.newNickname(), requiredStar);

        RewardHistory rewardHistory = new RewardHistory(user, requiredStar, rewardReason, userId);

        rewardService.save(rewardHistory);

        return NicknameChangeResponse.from(user);
    }

    private void validateEnoughStars(User user, int requiredStar) {
        if (user.canNotUseStars(requiredStar)) {
            throw new MomentException(ErrorCode.USER_NOT_ENOUGH_STAR);
        }
    }

    private void validateNicknameConflict(NicknameChangeRequest request) {
        if (userQueryService.existsByNickname(request.newNickname())) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }
}
