package moment.user.application;

import moment.user.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface UserQueryService {

    User getUserById(Long id);
}
