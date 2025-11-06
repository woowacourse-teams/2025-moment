package moment.moment.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.infrastructure.MomentImageRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.moment.infrastructure.MomentTagRepository;
import moment.moment.infrastructure.TagRepository;
import moment.support.MomentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@org.junit.jupiter.api.Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentApplicationServiceTest {

    @Autowired
    private MomentApplicationService momentApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private MomentCreatedAtHelper momentCreatedAtHelper;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MomentTagRepository momentTagRepository;

    @Autowired
    private MomentImageRepository momentImageRepository;

    @Test
    void 기본_모멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);

        MomentCreateRequest request = new MomentCreateRequest(
                "안녕하세요 반갑습니다.",
                List.of("일상/생각", "건강/운동"),
                "https://test.com/image.jpg",
                "image.jpg"
        );

        // when
        MomentCreateResponse response = momentApplicationService.createBasicMoment(request, momenter.getId());

        // then
        assertAll(
                () -> assertThat(response.momenterId()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("안녕하세요 반갑습니다.")
        );
    }

    @Test
    void 추가_모멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);
        int startPoint = 10;
        momenter.addStarAndUpdateLevel(startPoint);

        MomentCreateRequest request = new MomentCreateRequest(
                "안녕하세요 반갑습니다.",
                List.of("일상/생각", "건강/운동"),
                "https://test.com/image.jpg",
                "image.jpg"
        );

        // when
        MomentCreateResponse response = momentApplicationService.createExtraMoment(request, momenter.getId());

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.content()).isEqualTo("안녕하세요 반갑습니다.")
        );
    }

    @Test
    void 나의_모멘트_조합을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User momenter = userRepository.save(user);
        momenter.addStarAndUpdateLevel(40);

        Tag tag = tagRepository.save(new Tag("일상/생각"));

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Moment basicmoment = momentCreatedAtHelper.saveMomentWithCreatedAt("1", momenter, WriteType.BASIC, start);
        Moment extraMoment1 = momentCreatedAtHelper.saveMomentWithCreatedAt("2", momenter, WriteType.EXTRA,
                start.plusHours(1));
        Moment extraMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("3", momenter, WriteType.EXTRA,
                start.plusHours(2));
        Moment extraMoment3 = momentCreatedAtHelper.saveMomentWithCreatedAt("4", momenter, WriteType.EXTRA,
                start.plusHours(3));

        momentTagRepository.save(new MomentTag(basicmoment, tag));
        momentTagRepository.save(new MomentTag(extraMoment1, tag));
        momentTagRepository.save(new MomentTag(extraMoment2, tag));
        momentTagRepository.save(new MomentTag(extraMoment3, tag));

        String originalImageUrl = "https://test-bucket-1/test/images/photo2.png";
        momentImageRepository.save(new MomentImage(extraMoment2, originalImageUrl, "photo2.png"));

        Cursor cursor = new Cursor(extraMoment3.getCreatedAt().toString() + "_" + extraMoment3.getId());
        PageSize pageSize = new PageSize(2);

        // when
        MomentCompositions response = momentApplicationService.getMyMomentCompositions(cursor, pageSize,
                momenter.getId());

        // then
        String expectedResolvedUrl = "https://test-bucket-1/test/optimized-images/photo2";

        assertAll(
                () -> assertThat(response.momentCompositionInfo()).hasSize(2),
                () -> assertThat(response.nextCursor()).isNotNull(),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.momentCompositionInfo().getFirst().id()).isEqualTo(extraMoment2.getId()),
                () -> assertThat(response.momentCompositionInfo().getFirst().imageUrl()).isEqualTo(expectedResolvedUrl),
                () -> assertThat(response.momentCompositionInfo().getLast().id()).isEqualTo(extraMoment1.getId())
        );
    }
}
