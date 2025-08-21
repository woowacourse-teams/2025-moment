package moment.user.application;

import java.util.List;
import java.util.Optional;
import moment.user.domain.ProviderType;
import moment.user.domain.User;

public interface UserQueryService {

    User getUserById(Long id);

    boolean existsByNickname(String nickname);

    List<User> findNotMatchedUsersTodayByMomenter(User momenter);

    Optional<User> findUserByEmailAndProviderType(String email, ProviderType providerType);
}
