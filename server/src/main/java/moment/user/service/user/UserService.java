package moment.user.service.user;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
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
    public User addUser(String email, String password, String rePassword, String nickname) {
        comparePasswordWithRepassword(password, rePassword);
        validateEmailInBasicSignUp(email);
        validateNickname(nickname);

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, nickname, ProviderType.EMAIL);

        return userRepository.save(user);
    }

    private void comparePasswordWithRepassword(String password, String rePassword) {
        if (!password.equals(rePassword)) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    private void validateEmailInBasicSignUp(String email) {
        if (userRepository.existsByEmailAndProviderType(email, ProviderType.EMAIL)) {
            throw new MomentException(ErrorCode.USER_CONFLICT);
        }
    }

    public UserProfileResponse getUserProfileBy(Authentication authentication) {
        User user = getUserBy(authentication.id());
        return UserProfileResponse.from(user);
    }

    public NicknameConflictCheckResponse checkNicknameConflict(String nickname) {
        boolean existsByNickname = userRepository.existsByNickname(nickname);
        return new NicknameConflictCheckResponse(existsByNickname);
    }

    public User getUserBy(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
    }

    public Optional<User> findUserBy(String email, ProviderType providerType) {
        return userRepository.findByEmailAndProviderType(email, providerType);
    }

    public List<User> getAllBy(List<Long> ids) {
        return userRepository.findAllByIdIn(ids);
    }

    public boolean existsBy(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
