package moment.user.application.tobe.user;

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
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
    }

    public List<User> getAllByIds(List<Long> ids) {
        return userRepository.findAllByIdIn(ids);
    }

}
