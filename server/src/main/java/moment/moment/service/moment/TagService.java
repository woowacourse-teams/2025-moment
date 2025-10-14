package moment.moment.service.moment;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        List<Tag> allByNameIn = tagRepository.findAllByNameIn(tagNames);

        Set<String> existTagNames = allByNameIn.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTagsToSave = tagNames.stream()
                .filter(tagName -> !existTagNames.contains(tagName))
                .map(Tag::new)
                .toList();

        if (!newTagsToSave.isEmpty()) {
            List<Tag> newTags = tagRepository.saveAll(newTagsToSave);
            allByNameIn.addAll(newTags);
        }

        return allByNameIn;
    }
}
