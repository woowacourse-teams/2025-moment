package moment.reward.infrastructure;

import moment.reward.domain.PointHistory;
import moment.reward.domain.Reason;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<PointHistory, Long> {

    boolean existsByUserAndReasonAndContentId(User user, Reason reason, Long contentId);
}
