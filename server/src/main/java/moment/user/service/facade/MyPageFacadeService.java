package moment.user.service.facade;

import lombok.RequiredArgsConstructor;
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {

    private final UserService userService;

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
}
