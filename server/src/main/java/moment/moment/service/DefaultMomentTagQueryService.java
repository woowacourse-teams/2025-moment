package moment.moment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.infrastructure.MomentTagRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultMomentTagQueryService implements MomentTagQueryService {

    private final MomentTagRepository momentTagRepository;

    @Override
    public List<MomentTag> getAllByMomentIn(List<Moment> moments) {
        return momentTagRepository.findAllByMomentIn(moments);
    }

    @Override
    public List<MomentTag> getAllByMoment(Moment moment) {
        return momentTagRepository.findAllByMoment(moment);
    }
}
