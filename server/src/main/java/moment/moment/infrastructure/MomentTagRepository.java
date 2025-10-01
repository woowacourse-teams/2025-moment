package moment.moment.infrastructure;

import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MomentTagRepository extends JpaRepository<MomentTag, Long> {

    @EntityGraph(attributePaths = {"moment", "tag"})
    List<MomentTag> findAllByMomentIn(List<Moment> moments);

    List<MomentTag> findAllByMoment(Moment savedMoment);

    void deleteAllByMoment(Moment moment);

    List<MomentTag> findAllByMomentId(Long momentId);

    List<MomentTag> findAllByMomentIdIn(List<Long> momentIds);
}
