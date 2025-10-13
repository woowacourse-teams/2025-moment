package moment.moment.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
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

    @Test
    void 기본_모멘트를_작성한다() {
        // given
        User user = new User("test@email.com", "password123", "nickname", ProviderType.EMAIL);
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
        User user = new User("test@email.com", "password123", "nickname", ProviderType.EMAIL);
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
        User user = new User("test@email.com", "password123", "nickname", ProviderType.EMAIL);
        User momenter = userRepository.save(user);
        momenter.addStarAndUpdateLevel(40);

        MomentCreateResponse basicMoment = momentApplicationService.createBasicMoment(
                new MomentCreateRequest("1", List.of("일상/생각"), null, null), momenter.getId());
        MomentCreateResponse extraMoment1 = momentApplicationService.createExtraMoment(
                new MomentCreateRequest("2", List.of("일상/생각"), null, null), momenter.getId());
        MomentCreateResponse extraMoment2 = momentApplicationService.createExtraMoment(
                new MomentCreateRequest("3", List.of("일상/생각"), null, null), momenter.getId());
        MomentCreateResponse extraMoment3 = momentApplicationService.createExtraMoment(
                new MomentCreateRequest("4", List.of("일상/생각"), null, null), momenter.getId());

        Cursor cursor = new Cursor(extraMoment3.createdAt().toString() + "_" + extraMoment3.id());
        PageSize pageSize = new PageSize(2);

        // when
        MomentCompositions response = momentApplicationService.getMyMomentCompositions(cursor, pageSize, momenter.getId());

        // then
        assertAll(
                () -> assertThat(response.momentCompositionInfo()).hasSize(2),
                () -> assertThat(response.nextCursor()).isNotNull(),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.momentCompositionInfo().getFirst().id()).isEqualTo(extraMoment2.id()),
                () -> assertThat(response.momentCompositionInfo().getLast().id()).isEqualTo(extraMoment1.id())
        );
    }
}
