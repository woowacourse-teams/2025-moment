package moment.reward.infrastructure;

import moment.reward.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<PointHistory, Long> {
}
