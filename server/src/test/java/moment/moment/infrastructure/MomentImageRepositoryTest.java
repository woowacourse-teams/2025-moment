package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.WriteType;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class MomentImageRepositoryTest {

    @Autowired
    MomentImageRepository momentImageRepository;

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void 모멘트로_모멘트_이미지를_찾아온다() {
        // given
        User momenter = new User("emial@meail.com", "1234!", "마아", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        Moment moment = new Moment("내용", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        String imageUrl = "https://s3:moment-dev/images/고양이.jpg";
        String imageName = "고양이.jpg";

        MomentImage momentImage = new MomentImage(savedMoment, imageUrl, imageName);

        MomentImage savedMomentImage = momentImageRepository.save(momentImage);

        // when
        Optional<MomentImage> result = momentImageRepository.findByMoment(savedMoment);

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getId()).isEqualTo(savedMomentImage.getId())
        );
    }

    @Test
    void 모멘트들로_모멘트_이미지들을_조회한다() {
        // given
        String momentContent1 = "재미있는 내용이네요.";
        String momentContent2 = "재미없는 내용이네요.";
        String momentContent3 = "하하 내용이네요.";

        String imageUrl = "https://s3:tech-course/moment-dev/images/cat.jpg";
        String imageName = "cat.jpg";

        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment1 = new Moment(momentContent1, savedMomenter, WriteType.BASIC);
        Moment moment2 = new Moment(momentContent2, savedMomenter, WriteType.BASIC);
        Moment moment3 = new Moment(momentContent3, savedMomenter, WriteType.BASIC);

        Moment savedMoment1 = momentRepository.save(moment1);
        Moment savedMoment2 = momentRepository.save(moment2);
        Moment savedMoment3 = momentRepository.save(moment3);

        List<Moment> moments = List.of(savedMoment1, savedMoment2, savedMoment3);

        MomentImage momentImage1 = new MomentImage(savedMoment1, imageUrl, imageName);
        MomentImage momentImage3 = new MomentImage(savedMoment3, imageUrl, imageName);

        MomentImage savedMomentImage1 = momentImageRepository.save(momentImage1);
        MomentImage savedMomentImage3 = momentImageRepository.save(momentImage3);

        List<MomentImage> expected = List.of(savedMomentImage1, savedMomentImage3);

        // when
        List<MomentImage> results = momentImageRepository.findAllByMomentIn(moments);

        // then
        assertThat(results).isEqualTo(expected);
    }

    @Test
    void 모멘트id로_모멘트_이미지를_삭제한다() {
        // given
        User momenter = new User("emial@meail.com", "1234!", "마아", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment1 = new Moment("내용1", savedMomenter, WriteType.BASIC);
        Moment moment2 = new Moment("내용2", savedMomenter, WriteType.BASIC);
        Moment savedMoment1 = momentRepository.save(moment1);
        Moment savedMoment2 = momentRepository.save(moment2);

        String imageUrl = "https://s3:moment-dev/images/고양이.jpg";
        String imageName = "고양이.jpg";

        MomentImage momentImage1 = new MomentImage(savedMoment1, imageUrl, imageName);
        MomentImage momentImage2 = new MomentImage(savedMoment2, imageUrl, imageName);
        momentImageRepository.save(momentImage1);
        momentImageRepository.save(momentImage2);

        // when
        momentImageRepository.deleteByMomentId(savedMoment1.getId());

        // then
        Optional<MomentImage> findResult1 = momentImageRepository.findByMoment(savedMoment1);
        Optional<MomentImage> findResult2 = momentImageRepository.findByMoment(savedMoment2);

        assertAll(
                () -> assertThat(findResult1).isEmpty(),
                () -> assertThat(findResult2).isPresent()
        );
    }
}
