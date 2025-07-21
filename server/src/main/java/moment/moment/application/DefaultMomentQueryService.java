package moment.moment.application;

import moment.moment.domain.Moment;
import org.springframework.stereotype.Service;

@Service
public class DefaultMomentQueryService implements MomentQueryService {

    @Override
    public Moment getMomentById(Long id) {
        return null;
    }
}
