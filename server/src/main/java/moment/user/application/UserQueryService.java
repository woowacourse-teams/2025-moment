package moment.user.application;

import java.util.List;
import moment.user.domain.User;

public interface UserQueryService {

    User getUserById(Long id);

    boolean existsById(Long id);

    List<User> findAll();
}
