package moment.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndProviderType(String email, ProviderType providerType);

    Optional<User> findByEmailAndProviderType(String email, ProviderType providerType);

    @Query("""
    SELECT u FROM users u
    WHERE u != :momenter
      AND NOT EXISTS (
          SELECT 1 FROM matchings m
          WHERE m.commenter = u
            AND m.createdAt >= :startOfDay AND m.createdAt < :endOfDay
      )
    """)
    List<User> findNotMatchedUsersToday(@Param("startOfDay") LocalDateTime startOfDay,
                                        @Param("endOfDay") LocalDateTime endOfDay,
                                        @Param("momenter") User momenter);
}
