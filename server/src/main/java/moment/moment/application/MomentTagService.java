package moment.moment.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.infrastructure.MomentTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentTagService {

    private final MomentTagRepository momentTagRepository;

    @Transactional
    public MomentTag save(Moment moment, Tag tag) {
        return momentTagRepository.save(new MomentTag(moment, tag));
    }

    public Map<Moment, List<MomentTag>> getMomentTagsByMoment(List<Moment> moments) {
        List<MomentTag> momentTags = momentTagRepository.findAllByMomentIn(moments);

        return momentTags.stream()
                .collect(Collectors.groupingBy(MomentTag::getMoment));
    }
}
