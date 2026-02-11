package moment.user.service.user;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.infrastructure.GroupRepository;
import moment.notification.infrastructure.Emitters;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserWithdrawService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final Emitters emitters;

    public void validateWithdrawable(Long userId) {
        if (!groupRepository.findByOwnerId(userId).isEmpty()) {
            throw new MomentException(ErrorCode.USER_HAS_OWNED_GROUP);
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));

        pushNotificationRepository.deleteAllByUserId(userId);
        emitters.remove(userId);
        userRepository.delete(user);
    }
}
