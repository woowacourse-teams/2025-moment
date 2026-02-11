package moment.auth.infrastructure;

import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    boolean existsByUser(User user);

    void deleteByUser(User user);

    Optional<RefreshToken> findByTokenValue(String token);
}
