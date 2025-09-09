package moment.moment.application;

import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;

public interface MomentTagQueryService {

    List<MomentTag> getAllByMomentIn(List<Moment> moments);

    List<MomentTag> getAllByMoment(Moment moment);
}
