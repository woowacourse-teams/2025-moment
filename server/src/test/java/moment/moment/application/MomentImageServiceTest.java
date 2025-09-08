package moment.moment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.infrastructure.MomentImageRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MomentImageServiceTest {

    @InjectMocks
    MomentImageService momentImageService;

    @Mock
    MomentImageRepository momentImageRepository;

    @Test
    void 모멘트_이미지를_저장한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = "cat.jpg";

        MomentCreateRequest request = new MomentCreateRequest(momentContent, imageUrl, imageName);

        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment moment = new Moment(momentContent, momenter, WriteType.BASIC);
        MomentImage momentImage = new MomentImage(moment, imageUrl, imageName);

        given(momentImageRepository.save(any(MomentImage.class))).willReturn(momentImage);

        // when
        Optional<MomentImage> createdImage = momentImageService.create(request, moment);

        // then
        assertAll(
                () -> assertThat(createdImage).isPresent(),
                () -> assertThat(createdImage.get()).isEqualTo(momentImage),
                () -> then(momentImageRepository).should(times(1)).save(any(MomentImage.class))
        );
    }

    @Test
    void 모멘트_이미지_정보가_없으면_모멘트_이미지를_저장하지_않는다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        String imageUrl = null;
        String imageName = "cat.jpg";

        MomentCreateRequest request = new MomentCreateRequest(momentContent, imageUrl, imageName);

        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment moment = new Moment(momentContent, momenter, WriteType.BASIC);

        // when
        Optional<MomentImage> createdImage = momentImageService.create(request, moment);

        // then
        assertAll(
                () -> assertThat(createdImage).isEmpty(),
                () -> then(momentImageRepository).should(times(0)).save(any(MomentImage.class))
        );
    }
}
