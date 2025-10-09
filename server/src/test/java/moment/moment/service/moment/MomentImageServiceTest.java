package moment.moment.service.moment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentImageRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
class MomentImageServiceTest {

    @Autowired
    MomentImageService momentImageService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    MomentImageRepository momentImageRepository;

    private User momenter;

    @BeforeEach
    void setUp() {
        User user = new User("test@email.com", "password123", "nickname", ProviderType.EMAIL);
        momenter = userRepository.save(user);
    }

    @Test
    void 모멘트_이미지를_생성한다() {
        // given
        Moment savedMoment = momentRepository.save(new Moment("hello!", momenter, WriteType.BASIC));
        String imageUrl = "https://test.com/image.jpg";
        String imageName = "image.jpg";

        // when
        Optional<MomentImage> momentImage = momentImageService.create(savedMoment, imageUrl, imageName);

        // then
        assertAll(
                () -> assertThat(momentImage).isPresent(),
                () -> assertThat(momentImage.get().getMoment()).isEqualTo(savedMoment)
        );
    }

    @Test
    void 이미지_없는_경우_모멘트_이미지는_생성하지_않는다() {
        // given
        Moment savedMoment = momentRepository.save(new Moment("hello!", momenter, WriteType.BASIC));
        String imageUrl = null;
        String imageName = null;

        // when
        Optional<MomentImage> momentImage = momentImageService.create(savedMoment, imageUrl, imageName);

        // then
        assertThat(momentImage).isEmpty();
    }

    @Test
    void 모멘트에_달린_모멘트_이미지를_조회한다() {
        // given
        Moment noImageMoment = momentRepository.save(new Moment("no Image in here!", momenter, WriteType.BASIC));
        Moment imageMoment = momentRepository.save(new Moment("I have Image!", momenter, WriteType.BASIC));

        String imageUrl = "https://test.com/image.jpg";
        String imageName = "image.jpg";
        momentImageRepository.save(new MomentImage(imageMoment, imageUrl, imageName));

        List<Moment> moments = List.of(noImageMoment, imageMoment);

        // when
        Map<Moment, MomentImage> momentImageByMoment = momentImageService.getMomentImageByMoment(moments);

        // then
        assertAll(
                () -> assertThat(momentImageByMoment.size()).isEqualTo(1),
                () -> assertThat(momentImageByMoment.get(imageMoment)).isNotNull(),
                () -> assertThat(momentImageByMoment.get(noImageMoment)).isNull()
        );
    }

    @Test
    void 이미지가_없는_모멘트는_모멘트_이미지를_조회할_수_없다() {
        // given
        Moment noImageMoment = momentRepository.save(new Moment("no Image in here!", momenter, WriteType.BASIC));

        // when
        Optional<MomentImage> momentImage = momentImageService.findMomentImage(noImageMoment);

        // then
        assertThat(momentImage).isEmpty();
    }

    @Test
    void 이미지가_있는_모멘트는_모멘트_이미지를_조회할_수_있다() {
        // given
        Moment imageMoment = momentRepository.save(new Moment("I have Image!", momenter, WriteType.BASIC));
        String imageUrl = "https://test.com/image.jpg";
        String imageName = "image.jpg";
        momentImageRepository.save(new MomentImage(imageMoment, imageUrl, imageName));

        // when
        Optional<MomentImage> momentImage = momentImageService.findMomentImage(imageMoment);

        //then
        assertAll(
                () -> assertThat(momentImage).isPresent(),
                () -> assertThat(momentImage.get().getImageUrl()).isEqualTo(imageUrl)
        );
    }

    @Test
    void 모멘트_이미지를_삭제한다() {
        // given
        Moment imageMoment = momentRepository.save(new Moment("I have Image!", momenter, WriteType.BASIC));
        String imageUrl = "https://test.com/image.jpg";
        String imageName = "image.jpg";
        momentImageRepository.save(new MomentImage(imageMoment, imageUrl, imageName));

        // when
        momentImageService.deleteBy(imageMoment.getId());

        // then
        Optional<MomentImage> byMoment = momentImageRepository.findByMoment(imageMoment);
        assertThat(byMoment).isEmpty();
    }
}
