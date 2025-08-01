package moment.user.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultUserQueryService implements UserQueryService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public List<User> findNotMatchedUsersTodayByMomenter(User momenter) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        return userRepository.findNotMatchedUsersToday(startOfDay, endOfDay, momenter);
    }
}
