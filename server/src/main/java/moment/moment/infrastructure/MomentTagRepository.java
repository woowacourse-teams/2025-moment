package moment.moment.infrastructure;

import moment.moment.domain.MomentTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentTagRepository extends JpaRepository<MomentTag, Long> {
}
