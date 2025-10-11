package moment.user.service.user;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.NicknameConflictCheckRequest;
import moment.user.dto.request.UserCreateRequest;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User addUser(UserCreateRequest request) {
        comparePasswordWithRepassword(request.password(), request.rePassword());
        validateEmailInBasicSignUp(request);
        validateNickname(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname(), ProviderType.EMAIL);

        return userRepository.save(user);
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
        User user = getUserById(authentication.id());
        return UserProfileResponse.from(user);
    }

    public NicknameConflictCheckResponse checkNicknameConflict(NicknameConflictCheckRequest request) {
        boolean existsByNickname = userRepository.existsByNickname(request.nickname());
        return new NicknameConflictCheckResponse(existsByNickname);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
    }

    public Optional<User> findUserByEmailAndProviderType(String email, ProviderType providerType) {
        return userRepository.findByEmailAndProviderType(email, ProviderType.EMAIL);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> getAllByIds(List<Long> ids) {
        return userRepository.findAllByIdIn(ids);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
