package moment.user.application;

import moment.user.domain.ProviderType;
import moment.user.domain.User;

import java.util.List;

public interface UserQueryService {

    User getUserById(Long id);

    boolean existsByNickname(String nickname);

    List<User> findNotMatchedUsersTodayByMomenter(User momenter);

    User getUserByEmailAndProviderType(String email, ProviderType providerType);
}
