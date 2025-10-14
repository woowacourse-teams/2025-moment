package moment.moment.service.moment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.moment.domain.Tag;
import moment.moment.infrastructure.TagRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void 요청한_태그가_모두_존재하지_않으면_새로_생성하고_반환한다() {
        // given
        List<String> tagNames = List.of("일상/생각", "건강/운동");

        // when
        List<Tag> tags = tagService.getOrCreate(tagNames);

        // then
        List<Tag> allTags = tagRepository.findAll();
        assertAll(
                () -> assertThat(tags).hasSize(2),
                () -> assertThat(allTags).hasSize(2),
                () -> assertThat(tags.stream().map(Tag::getName))
                        .containsExactlyInAnyOrder("일상/생각", "건강/운동")
        );
    }

    @Test
    void 일부_태그만_존재하면_없는_태그만_생성한_후_모두_반환한다() {
        // given
        tagRepository.save(new Tag("일상/생각"));
        List<String> tagNames = List.of("일상/생각", "건강/운동");

        // when
        List<Tag> tags = tagService.getOrCreate(tagNames);

        // then
        List<Tag> allTags = tagRepository.findAll();
        assertAll(
                () -> assertThat(tags).hasSize(2),
                () -> assertThat(allTags).hasSize(2),
                () -> assertThat(tags.stream().map(Tag::getName))
                        .containsExactlyInAnyOrder("일상/생각", "건강/운동")
        );
    }

    @Test
    void 요청한_태그가_모두_존재하면_기존_태그를_조회하여_반환한다() {
        // given
        List<Tag> existingTags = tagRepository.saveAll(List.of(new Tag("일상/생각"), new Tag("건강/운동")));
        List<String> tagNames = List.of("일상/생각", "건강/운동");

        // when
        List<Tag> tags = tagService.getOrCreate(tagNames);

        // then
        List<Tag> allTags = tagRepository.findAll();
        assertAll(
                () -> assertThat(tags).hasSize(2),
                () -> assertThat(allTags).hasSize(2),
                () -> assertThat(tags.stream().map(Tag::getName))
                        .containsExactlyInAnyOrder("일상/생각", "건강/운동")
        );
    }

}
