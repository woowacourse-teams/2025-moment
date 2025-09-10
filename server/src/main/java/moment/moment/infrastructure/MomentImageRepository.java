package moment.moment.infrastructure;

import java.util.Optional;
import moment.moment.domain.MomentImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentImageRepository extends JpaRepository<MomentImage, Long> {

    Optional<MomentImage> findByMomentId(Long momentId);
}
