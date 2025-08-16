package moment.reward.infrastructure;

import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RewardRepository extends JpaRepository<RewardHistory, Long> {

    boolean existsByUserAndReasonAndContentId(User user, Reason reason, Long contentId);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT COUNT(rh) > 0 FROM reward_history rh
            WHERE rh.user = :user AND rh.reason = :reason AND rh.createdAt >= :startOfToday AND rh.createdAt < :endOfToday
            """
    )
    boolean existsByUserAndReasonAndToday(@Param("user") User user,
                                          @Param("reason") Reason reason,
                                          @Param("startOfToday") LocalDateTime startOfToday,
                                          @Param("endOfToday") LocalDateTime endOfToday);
}
