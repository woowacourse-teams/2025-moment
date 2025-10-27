package moment.moment.service.moment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.moment.infrastructure.MomentTagRepository;
import moment.moment.infrastructure.TagRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class MomentTagServiceTest {

    @Autowired
    private MomentTagService momentTagService;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private MomentTagRepository momentTagRepository;

    private User momenter;

    @BeforeEach
    void setUp() {
        User user = new User("test@email.com", "password123", "nickname", ProviderType.EMAIL);
        momenter = userRepository.save(user);
    }

    @Test
    void 모멘트_태그를_저장한다() {
        // given
        Moment savedMoment = momentRepository.save(new Moment("hello", momenter, WriteType.BASIC));

        List<Tag> tags = List.of(new Tag("일상/생각"), new Tag("건강/운동"));
        tagRepository.saveAll(tags);

        // when
        List<MomentTag> momentTags = momentTagService.createAll(savedMoment, tags);

        // then
        assertAll(
                () -> assertThat(momentTags).hasSize(2),
                () -> assertThat(momentTags.getFirst().getTagName()).isEqualTo("일상/생각"),
                () -> assertThat(momentTags.getLast().getTagName()).isEqualTo("건강/운동")
        );
    }

    @Test
    void 모멘트에_달린_모멘트_태그를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter, WriteType.BASIC));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter, WriteType.BASIC));

        List<Moment> moments = List.of(moment1, moment2, moment3);

        Tag life = new Tag("일상/생각");
        Tag health = new Tag("건강/운동");
        List<Tag> tags = List.of(life, health);
        tagRepository.saveAll(tags);

        List<MomentTag> momentTags1 = momentTagRepository.saveAll(
                List.of(new MomentTag(moment1, life), new MomentTag(moment1, health)));
        List<MomentTag> momentTags2 = momentTagRepository.saveAll(List.of(new MomentTag(moment2, life)));
        List<MomentTag> momentTags3 = momentTagRepository.saveAll(List.of(new MomentTag(moment3, health)));

        // when
        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTagService.getMomentTagsByMoment(moments);

        // then
        assertAll(
                () -> assertThat(momentTagsByMoment).hasSize(3),
                () -> assertThat(momentTagsByMoment.get(moment1)).isEqualTo(momentTags1),
                () -> assertThat(momentTagsByMoment.get(moment2)).isEqualTo(momentTags2),
                () -> assertThat(momentTagsByMoment.get(moment3)).isEqualTo(momentTags3)
        );
    }

    @Test
    void 태그_이름으로_모멘트_ID를_조회한다() {
        // given
        Moment moment1 = momentRepository.save(new Moment("moment1", momenter, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("moment2", momenter, WriteType.BASIC));
        Moment moment3 = momentRepository.save(new Moment("moment3", momenter, WriteType.BASIC));
        List<Long> allMomentIds = List.of(moment1.getId(), moment2.getId(), moment3.getId());

        Tag life = new Tag("일상/생각");
        Tag health = new Tag("건강/운동");
        Tag trip = new Tag("여행");
        tagRepository.saveAll(List.of(life, health, trip));

        momentTagRepository.saveAll(
                List.of(new MomentTag(moment1, life), new MomentTag(moment1, health),
                        new MomentTag(moment2, life), new MomentTag(moment3, trip)));

        // when
        List<Long> foundMomentIds = momentTagService.getMomentIdsByTags(allMomentIds,
                List.of("일상/생각", "건강/운동"));

        // then
        assertThat(foundMomentIds).containsExactlyInAnyOrder(moment1.getId(), moment2.getId());
    }

    @Test
    void 모멘트_ID로_모멘트_태그를_삭제한다() {
        // given
        Moment moment = momentRepository.save(new Moment("moment1", momenter, WriteType.BASIC));

        Tag life = new Tag("일상/생각");
        Tag health = new Tag("건강/운동");
        tagRepository.saveAll(List.of(life, health));

        momentTagRepository.saveAll(List.of(new MomentTag(moment, life), new MomentTag(moment, health)));

        // when
        momentTagService.deleteBy(moment.getId());

        // then
        List<MomentTag> momentTags = momentTagRepository.findAllWithTagsByMomentIn(List.of(moment));
        assertThat(momentTags).isEmpty();
    }
}
