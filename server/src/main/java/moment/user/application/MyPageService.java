package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.reward.application.RewardService;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
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
}
