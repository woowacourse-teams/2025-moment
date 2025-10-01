package moment.moment.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.moment.domain.Tag;
import moment.moment.infrastructure.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag getOrRegister(String name) {
        Optional<Tag> tag = tagRepository.findByName(name);
        return tag.orElseGet(() -> tagRepository.save(new Tag(name)));
    }
}
