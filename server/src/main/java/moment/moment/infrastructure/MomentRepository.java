package moment.moment.infrastructure;

import moment.moment.domain.Moment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentRepository extends JpaRepository<Moment, Long> {
}
