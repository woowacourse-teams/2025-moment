package moment.moment.service.tobe.moment;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Tag;
import moment.moment.infrastructure.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    
    @Transactional
    public List<Tag> getOrCreate(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            tagRepository.findByName(tagName)
                    .ifPresentOrElse(tags::add, () -> tagRepository.save(new Tag(tagName)));
        }
        return tags;
    }
}
