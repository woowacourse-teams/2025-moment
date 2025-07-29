package moment.user.application;

import moment.user.domain.User;

import java.util.List;

public interface UserQueryService {

    User getUserById(Long id);

    boolean existsById(Long id);

    List<User> findNotMatchedUsersTodayByMomenter(User momenter);
}
