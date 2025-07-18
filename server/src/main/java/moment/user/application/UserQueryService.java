package moment.user.application;

import moment.user.domain.User;

public interface UserQueryService {

    User getUserById(Long id);
}
