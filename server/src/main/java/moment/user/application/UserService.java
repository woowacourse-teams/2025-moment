package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
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
        comparePasswordWithRepassword(request);
        validateEmail(request);
        validateNickname(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname(), ProviderType.EMAIL);

        userRepository.save(user);
    }

    private void comparePasswordWithRepassword(UserCreateRequest request) {
        if (!request.password().equals(request.rePassword())) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateNickname(UserCreateRequest request) {
        if (userRepository.existsByNickname(request.nickname())) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    private void validateEmail(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
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

    public EmailConflictCheckResponse checkEmailConflict(EmailConflictCheckRequest request) {
        boolean existsByEmail = userRepository.existsByEmail(request.email());
        return new EmailConflictCheckResponse(existsByEmail);
    }
}
