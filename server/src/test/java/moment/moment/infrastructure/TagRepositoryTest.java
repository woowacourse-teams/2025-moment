package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import moment.config.TestTags;
import moment.moment.domain.Tag;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@org.junit.jupiter.api.Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void 이름_리스트에_포함된_모든_태그를_조회한다() {
        // given
        Tag tag1 = tagRepository.save(new Tag("일상"));
        tagRepository.save(new Tag("운동"));
        Tag tag3 = tagRepository.save(new Tag("여행"));

        List<String> namesToFind = List.of("일상", "여행", "음악");

        // when
        List<Tag> foundTags = tagRepository.findAllByNameIn(namesToFind);

        // then
        assertThat(foundTags).hasSize(2)
                .containsExactlyInAnyOrder(tag1, tag3);
    }

    @Test
    void 이름_리스트가_비어있으면_빈_리스트를_반환한다() {
        // given
        tagRepository.save(new Tag("일상"));
        tagRepository.save(new Tag("운동"));

        // when
        List<Tag> foundTags = tagRepository.findAllByNameIn(Collections.emptyList());

        // then
        assertThat(foundTags).isEmpty();
    }
}
