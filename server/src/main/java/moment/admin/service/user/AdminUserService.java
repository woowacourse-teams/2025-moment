package moment.admin.service.user;

import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = getUserById(userId);

        if (!user.getNickname().equals(request.nickname())) {
            user.updateNickname(request.nickname(), 0);
        }

        user.updateStarsDirectly(request.availableStar(), request.expStar());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        userRepository.flush();
    }
}