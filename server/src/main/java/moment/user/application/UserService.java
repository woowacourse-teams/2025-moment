package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.EmailConflictCheckRequest;
import moment.user.dto.request.NicknameConflictCheckRequest;
import moment.user.dto.request.UserCreateRequest;
import moment.user.dto.response.EmailConflictCheckResponse;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.dto.response.NicknameConflictCheckResponse;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserQueryService userQueryService;
    private final UserRepository userRepository;
    private final NicknameGenerateService nicknameGenerateService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void addUser(UserCreateRequest request) {
        comparePasswordWithRepassword(request.password(), request.rePassword());
        validateEmailInBasicSignUp(request);
        validateNickname(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname(), ProviderType.EMAIL);

        userRepository.save(user);
    }

    private void comparePasswordWithRepassword(String password, String rePassword) {
        if (!password.equals(rePassword)) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateNickname(UserCreateRequest request) {
        if (userRepository.existsByNickname(request.nickname())) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    private void validateEmailInBasicSignUp(UserCreateRequest request) {
        if (userRepository.existsByEmailAndProviderType(request.email(), ProviderType.EMAIL)) {
            throw new MomentException(ErrorCode.USER_CONFLICT);
        }
    }

    public UserProfileResponse getUserProfile(Authentication authentication) {
        User user = userQueryService.getUserById(authentication.id());
        return UserProfileResponse.from(user);
    }

    public MomentRandomNicknameResponse createRandomNickname() {
        String randomNickname = nicknameGenerateService.createRandomNickname();
        return new MomentRandomNicknameResponse(randomNickname);
    }

    public NicknameConflictCheckResponse checkNicknameConflict(NicknameConflictCheckRequest request) {
        boolean existsByNickname = userRepository.existsByNickname(request.nickname());
        return new NicknameConflictCheckResponse(existsByNickname);
    }

    public EmailConflictCheckResponse checkEmailConflictInBasicSignUp(EmailConflictCheckRequest request) {
        boolean existsByEmail = userRepository.existsByEmailAndProviderType(request.email(), ProviderType.EMAIL);
        return new EmailConflictCheckResponse(existsByEmail);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userQueryService.getUserById(userId);

        validateChangeablePasswordUser(user);
        comparePasswordWithRepassword(request.password(), request.rePassword());

        String encodedChangePassword = passwordEncoder.encode(request.password());
        validateNotSameAsOldPassword(user, encodedChangePassword);

        user.changePassword(encodedChangePassword);
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
