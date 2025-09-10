package moment.moment.infrastructure;

import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MomentImageRepository extends JpaRepository<MomentImage, Long> {

    @Query("SELECT mi FROM moment_images mi WHERE mi.moment = :moment")
    Optional<MomentImage> findMomentImageByMoment(@Param("moment")Moment moment);
}
