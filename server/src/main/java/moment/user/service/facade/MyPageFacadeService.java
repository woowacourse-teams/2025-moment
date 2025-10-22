package moment.user.service.facade;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.Reason;
import moment.reward.service.application.RewardApplicationService;
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {

    private final RewardApplicationService rewardApplicationService;
    private final UserService userService;

    public MyRewardHistoryPageResponse getMyRewardHistory(Long userId, int pageNum, int pageSize) {
        return rewardApplicationService.getRewardHistoryBy(userId, pageNum, pageSize);
    }

    public MyPageProfileResponse getMyProfile(Long userId) {
        User user = userService.getUserBy(userId);
        return MyPageProfileResponse.from(user);
    }

    @Transactional
    public NicknameChangeResponse changeNickname(NicknameChangeRequest request, Long userId) {
        NicknameChangeResponse nicknameChangeResponse = userService.changeNickname(request.newNickname(), userId);
        rewardApplicationService.saveRewardHistory(Reason.NICKNAME_CHANGE, userId);
        return nicknameChangeResponse;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        userService.changePassword(request.newPassword(), request.checkedPassword(), userId);
    }
}
