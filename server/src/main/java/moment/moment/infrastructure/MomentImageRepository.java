package moment.moment.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentImageRepository extends JpaRepository<MomentImage, Long> {

    Optional<MomentImage> findByMoment(Moment moment);

    @EntityGraph(attributePaths = {"moment"})
    List<MomentImage> findAllByMomentIn(List<Moment> moments);

    void deleteByMoment(Moment moment);
}
