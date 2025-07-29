package moment.moment.application;

import java.util.Optional;
import moment.moment.domain.Moment;
import moment.user.domain.User;

public interface MomentQueryService {

    Moment getMomentById(Long id);

    Optional<Moment> findTodayMatchedMomentByCommenter(User commenter);
}
