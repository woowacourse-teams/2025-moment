package moment.moment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.comment.service.CommentImageService;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.infrastructure.CommentImageRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
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
class CommentImageServiceTest {

    @InjectMocks
    CommentImageService commentImageService;

    @Mock
    CommentImageRepository commentImageRepository;

    @Test
    void 코멘트_이미지를_저장한다() {
        // given
        String momentContent = "굿";
        String commentContent = "재미있는 내용이네요.";
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = "cat.jpg";

        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        User commenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);
        Moment moment = new Moment(momentContent, momenter, WriteType.BASIC);

        Comment comment = new Comment(commentContent, commenter, moment);

        CommentCreateRequest request = new CommentCreateRequest(commentContent, 1L, imageUrl, imageName);

        CommentImage commentImage = new CommentImage(comment, imageUrl, imageName);

        given(commentImageRepository.save(any(CommentImage.class))).willReturn(commentImage);

        // when
        Optional<CommentImage> createdImage = commentImageService.create(request, comment);

        // then
        assertAll(
                () -> assertThat(createdImage).isPresent(),
                () -> assertThat(createdImage.get()).isEqualTo(commentImage),
                () -> then(commentImageRepository).should(times(1)).save(any(CommentImage.class))
        );
    }

    @Test
    void 코멘트_이미지_정보가_없으면_코멘트_이미지를_저장하지_않는다() {
        // given
        String momentContent = "굿";
        String commentContent = "재미있는 내용이네요.";
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = null;

        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        User commenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);
        Moment moment = new Moment(momentContent, momenter, WriteType.BASIC);

        Comment comment = new Comment(commentContent, commenter, moment);

        CommentCreateRequest request = new CommentCreateRequest(commentContent, 1L, imageUrl, imageName);

        // when
        Optional<CommentImage> createdImage = commentImageService.create(request, comment);

        // then
        assertAll(
                () -> assertThat(createdImage).isEmpty(),
                () -> then(commentImageRepository).should(times(0)).save(any(CommentImage.class))
        );
    }
}
