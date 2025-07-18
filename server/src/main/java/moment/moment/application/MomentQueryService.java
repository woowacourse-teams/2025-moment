package moment.moment.application;

import moment.moment.domain.Moment;

public interface MomentQueryService {

    Moment getMomentById(Long id);
}
