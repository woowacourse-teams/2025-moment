package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.UserCreateRequest;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserQueryService userQueryService;
    private final UserRepository userRepository;

    @Transactional
    public void addUser(UserCreateRequest request) {
        comparePasswordWithRepassword(request);
        validateEmail(request);
        validateNickname(request);
        userRepository.save(request.toUser());
    }

    private void comparePasswordWithRepassword(UserCreateRequest request) {
        if (!request.password().equals(request.rePassword())) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateEmail(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new MomentException(ErrorCode.USER_CONFLICT);
        }
    }

    private void validateNickname(UserCreateRequest request) {
        if (userRepository.existsByNickname(request.nickname())) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    public UserProfileResponse getUserProfile(Authentication authentication) {
        User user = userQueryService.getUserById(authentication.id());
        return UserProfileResponse.from(user);
    }
}
