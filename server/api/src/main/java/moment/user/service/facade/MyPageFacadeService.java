package moment.user.service.facade;

import lombok.RequiredArgsConstructor;
import moment.auth.service.auth.AuthService;
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.service.user.UserService;
import moment.user.service.user.UserWithdrawService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {

    private final UserService userService;
    private final UserWithdrawService userWithdrawService;
    private final AuthService authService;

    public MyPageProfileResponse getMyProfile(Long userId) {
        User user = userService.getUserBy(userId);
        return MyPageProfileResponse.from(user);
    }

    @Transactional
    public NicknameChangeResponse changeNickname(NicknameChangeRequest request, Long userId) {
        return userService.changeNickname(request.newNickname(), userId);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        userService.changePassword(request.newPassword(), request.checkedPassword(), userId);
    }

    @Transactional
    public void withdraw(Long userId) {
        userWithdrawService.validateWithdrawable(userId);
        authService.logout(userId);
        userWithdrawService.withdraw(userId);
    }
}
