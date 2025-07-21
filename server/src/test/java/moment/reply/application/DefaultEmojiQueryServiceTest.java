package moment.reply.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;
import moment.reply.infrastructure.EmojiRepository;
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
class DefaultEmojiQueryServiceTest {

    @Mock
    EmojiRepository emojiRepository;

    @InjectMocks
    DefaultEmojiQueryService defaultEmojiQueryService;

    @Test
    void id로_이모지를_조회한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Emoji emoji = new Emoji(EmojiType.HEART, momenter, comment);

        given(emojiRepository.findById(any(Long.class)))
                .willReturn(Optional.of(emoji));

        // when
        defaultEmojiQueryService.getEmojiById(1L);

        // then
        then(emojiRepository).should(times(1)).findById(any(Long.class));
    }
}