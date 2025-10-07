package moment.moment.service.moment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.infrastructure.MomentTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentTagService {

    private final MomentTagRepository momentTagRepository;

    @Transactional
    public MomentTag save(Moment moment, Tag tag) {
        return momentTagRepository.save(new MomentTag(moment, tag));
    }

    @Transactional
    public List<MomentTag> createAll(Moment savedMoment, List<Tag> tags) {
        for (Tag tag : tags) {
            momentTagRepository.save(new MomentTag(savedMoment, tag));
        }
        return momentTagRepository.findAllByMoment(savedMoment);
    }

    public Map<Moment, List<MomentTag>> getMomentTagsByMoment(List<Moment> moments) {
        return momentTagRepository.findAllByMomentIn(moments).stream()
                .collect(Collectors.groupingBy(MomentTag::getMoment));
    }

    public List<Long> getMomentIdsByTags(List<Long> momentIds, List<String> tagNames) {
        return momentTagRepository.findAllMomentIdByTagNamesIn(momentIds, tagNames);
    }

    @Transactional
    public void deleteBy(Long momentId) {
        momentTagRepository.deleteByMomentId(momentId);
    }
}
