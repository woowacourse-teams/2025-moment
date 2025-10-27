package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentTagRepositoryTest {

    @Autowired
    private MomentTagRepository momentTagRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Moment moment1, moment2, moment3;
    private Tag tagLife, tagHealth, tagTrip;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("test@email.com", "1234", "tester", ProviderType.EMAIL));

        moment1 = momentRepository.save(new Moment("moment1", user, WriteType.BASIC));
        moment2 = momentRepository.save(new Moment("moment2", user, WriteType.BASIC));
        moment3 = momentRepository.save(new Moment("moment3", user, WriteType.BASIC));

        tagLife = tagRepository.save(new Tag("일상"));
        tagHealth = tagRepository.save(new Tag("운동"));
        tagTrip = tagRepository.save(new Tag("여행"));
    }

    @Test
    void 모멘트_리스트로_연관된_모멘트태그를_모두_조회한다() {
        // given
        MomentTag momentTag1 = momentTagRepository.save(new MomentTag(moment1, tagLife));
        MomentTag momentTag2 = momentTagRepository.save(new MomentTag(moment2, tagHealth));
        momentTagRepository.save(new MomentTag(moment3, tagTrip));

        // when
        List<MomentTag> results = momentTagRepository.findAllWithTagsByMomentIn(List.of(moment1, moment2));

        // then
        assertThat(results).hasSize(2)
                .containsExactlyInAnyOrder(momentTag1, momentTag2);
    }

    @Test
    void 태그_이름과_모멘트ID_리스트로_필터링된_모멘트ID를_조회한다() {
        // given
        momentTagRepository.save(new MomentTag(moment1, tagLife));
        momentTagRepository.save(new MomentTag(moment2, tagHealth));
        momentTagRepository.save(new MomentTag(moment3, tagTrip));

        List<Long> momentIdsToSearch = List.of(moment1.getId(), moment2.getId());
        List<String> tagNamesToSearch = List.of("일상", "운동");

        // when
        List<Long> results = momentTagRepository.findAllMomentIdByTagNamesIn(momentIdsToSearch, tagNamesToSearch);

        // then
        assertThat(results).hasSize(2)
                .containsExactlyInAnyOrder(moment1.getId(), moment2.getId());
    }



    @Test
    void 모멘트ID로_연관된_모멘트태그를_모두_삭제한다() {
        // given
        momentTagRepository.save(new MomentTag(moment1, tagLife));
        momentTagRepository.save(new MomentTag(moment1, tagHealth));
        MomentTag momentTagToKeep = momentTagRepository.save(new MomentTag(moment2, tagTrip));

        // when
        momentTagRepository.deleteByMomentId(moment1.getId());

        // then
        List<MomentTag> allMomentTags = momentTagRepository.findAll();
        assertThat(allMomentTags).hasSize(1)
                .containsExactly(momentTagToKeep);
    }

    @Test
    void 모멘트리스트로_조회할때_tags가_fetch_join되어_즉시로딩되는지_확인한다() {
        // given
        MomentTag mt1 = momentTagRepository.save(new MomentTag(moment1, tagLife));
        MomentTag mt2 = momentTagRepository.save(new MomentTag(moment2, tagHealth));
        momentTagRepository.save(new MomentTag(moment3, tagTrip));

        List<Moment> momentsToFetch = List.of(moment1, moment2);

        // when
        List<MomentTag> results = momentTagRepository.findAllWithTagsByMomentIn(momentsToFetch);

        // then
        assertThat(results).hasSize(2)
                .allSatisfy(mt -> assertThat(mt.getTag()).isNotNull());
    }
}
