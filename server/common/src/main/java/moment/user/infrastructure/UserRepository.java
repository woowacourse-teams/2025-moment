package moment.user.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndProviderType(String email, ProviderType providerType);

    Optional<User> findByEmailAndProviderType(String email, ProviderType providerType);

    List<User> findAllByIdIn(List<Long> ids);

    /**
     * 모든 사용자 조회 (soft delete된 것 포함, 페이징)
     * Native Query로 @SQLRestriction 우회
     */
    @Query(
        value = "SELECT * FROM users ORDER BY created_at DESC, id DESC",
        countQuery = "SELECT COUNT(*) FROM users",
        nativeQuery = true
    )
    Page<User> findAllIncludingDeleted(Pageable pageable);
}
