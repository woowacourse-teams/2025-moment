package moment.moment.infrastructure;

import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MomentTagRepository extends JpaRepository<MomentTag, Long> {

    List<MomentTag> findAllByMomentIn(List<Moment> moments);

    List<MomentTag> findAllByMoment(Moment savedMoment);

    void deleteAllByMoment(Moment moment);

    List<MomentTag> findAllByMomentId(Long momentId);

    List<MomentTag> findAllByMomentIdIn(List<Long> momentIds);

    @Query("""
            SELECT DISTINCT mt.moment.id
            FROM moment_tags mt
            JOIN tags t
            WHERE t.name IN :tagNames
                AND mt.moment.id IN :momentIds
            """)
    List<Long> findAllMomentIdByTagNamesIn(@Param("momentIds") List<Long> momentIds,
                                           @Param("tagNames") List<String> tagNames);

    void deleteByMomentId(Long momentId);
}
