package moment.user.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndProviderType(String email, ProviderType providerType);

    Optional<User> findByEmailAndProviderType(String email, ProviderType providerType);

    List<User> findAllByIdIn(List<Long> ids);
}
