package moment.user.service;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.service.reward.RewardService;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.service.user.UserService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.NicknameChangeResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserService userService;
    private final RewardService rewardService;
    private final PasswordEncoder passwordEncoder;

    public MyPageProfileResponse getProfile(Long userId) {
        User user = userService.getUserById(userId);
        return MyPageProfileResponse.from(user);
    }

    public MyRewardHistoryPageResponse getMyRewardHistory(Integer pageNum, Integer pageSize, Long userId) {

        User user = userService.getUserById(userId);

        return rewardService.getRewardHistoryByUser(user, pageNum, pageSize);
    }

    @Transactional
    public NicknameChangeResponse changeNickname(NicknameChangeRequest request, Long userId) {
        User user = userService.getUserById(userId);

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
        if (userService.existsByNickname(request.newNickname())) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userService.getUserById(userId);

        validateChangeablePasswordUser(user);
        comparePasswordWithRepassword(request.newPassword(), request.checkedPassword());

        String encodedChangePassword = passwordEncoder.encode(request.newPassword());
        validateNotSameAsOldPassword(user, encodedChangePassword);

        user.updatePassword(encodedChangePassword);
    }

    private void comparePasswordWithRepassword(String password, String rePassword) {
        if (!password.equals(rePassword)) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateNotSameAsOldPassword(User user, String encodedChangePassword) {
        if (user.checkPassword(encodedChangePassword)) {
            throw new MomentException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }
    }

    private void validateChangeablePasswordUser(User user) {
        if (!user.checkProviderType(ProviderType.EMAIL)) {
            throw new MomentException(ErrorCode.PASSWORD_CHANGE_UNSUPPORTED_PROVIDER);
        }
    }
}
