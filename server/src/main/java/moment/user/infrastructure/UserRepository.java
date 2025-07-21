package moment.user.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u
    FROM users u
    WHERE u.id NOT IN (
        SELECT m.commenter.id
        FROM matchings m
        WHERE FUNCTION('DATE', m.createdAt) = :today
    )
    AND u.id != :momenterId
    """)
    List<User> findNotMatchedUsersToday(@Param("today") LocalDate today, @Param("momenterId") Long momenterId);
}
