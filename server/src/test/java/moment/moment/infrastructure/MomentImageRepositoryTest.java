package moment.moment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
}
