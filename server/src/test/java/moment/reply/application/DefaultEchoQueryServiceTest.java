package moment.reply.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.comment.domain.Echo;
import moment.comment.infrastructure.EchoRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class DefaultEchoQueryServiceTest {

    @Mock
    EchoRepository echoRepository;

    @InjectMocks
    DefaultEchoQueryService defaultEmojiQueryService;

    @Test
    void id로_에코를_조회한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Echo echo = new Echo("HEART", momenter, comment);

        given(echoRepository.findById(any(Long.class)))
                .willReturn(Optional.of(echo));

        // when
        defaultEmojiQueryService.getEchoById(1L);

        // then
        then(echoRepository).should(times(1)).findById(any(Long.class));
    }
}
