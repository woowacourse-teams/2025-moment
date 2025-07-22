package moment.moment.infrastructure;

import java.util.List;
import moment.moment.domain.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    List<Moment> findMomentByMomenter_Id(Long id);
}
