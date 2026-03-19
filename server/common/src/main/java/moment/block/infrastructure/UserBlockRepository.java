package moment.block.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.block.domain.UserBlock;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByBlockerAndBlockedUser(User blocker, User blockedUser);

    boolean existsByBlockerAndBlockedUser(User blocker, User blockedUser);

    List<UserBlock> findAllByBlocker(User blocker);

    @Query("SELECT ub FROM UserBlock ub JOIN FETCH ub.blockedUser WHERE ub.blocker = :blocker")
    List<UserBlock> findAllByBlockerWithBlockedUser(@Param("blocker") User blocker);

    @Query(value = """
        SELECT blocked_user_id FROM user_blocks WHERE blocker_id = :userId AND deleted_at IS NULL
        UNION ALL
        SELECT blocker_id FROM user_blocks WHERE blocked_user_id = :userId AND deleted_at IS NULL
        """, nativeQuery = true)
    List<Long> findBlockedUserIds(@Param("userId") Long userId);

    @Query(value = """
        SELECT * FROM user_blocks
        WHERE blocker_id = :blockerId AND blocked_user_id = :blockedUserId
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserBlock> findByBlockerAndBlockedUserIncludeDeleted(
            @Param("blockerId") Long blockerId,
            @Param("blockedUserId") Long blockedUserId);

    @Query("""
        SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END
        FROM UserBlock ub
        WHERE (ub.blocker.id = :userId1 AND ub.blockedUser.id = :userId2)
           OR (ub.blocker.id = :userId2 AND ub.blockedUser.id = :userId1)
        """)
    boolean existsBidirectionalBlock(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}
