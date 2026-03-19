package moment.admin.service.user;

import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllIncludingDeleted(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = getUserById(userId);

        if (!user.getNickname().equals(request.nickname())) {
            user.updateNickname(request.nickname());
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        userRepository.flush();
    }
}
