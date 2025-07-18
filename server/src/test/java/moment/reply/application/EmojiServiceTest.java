package moment.reply.application;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class EmojiServiceTest {

    @Mock
    private EmojiRepository emojiRepository;

    @Mock
    private CommentQueryService commentQueryService;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private EmojiService emojiService;

    @Test
    void 코멘트에_이모지를_추가_할_수_있다() {
        // given
        Authentication authentication = new Authentication(1L);
        EmojiCreateRequest request = new EmojiCreateRequest("HEART", 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Emoji emoji = new Emoji(EmojiType.HEART, momenter, comment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);
        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);
        given(emojiRepository.save(any(Emoji.class)))
                .willReturn(emoji);

        // when
        emojiService.addEmoji(request, authentication);

        // then
        then(emojiRepository).should(times(1)).save(any(Emoji.class));
    }

    @Test
    void 존재하지_않는_이모지를_등록할_경우_예외가_발생한다() {
        // given
        Authentication authentication = new Authentication(1L);
        EmojiCreateRequest request = new EmojiCreateRequest("NO_EXIST_EMOJI", 1L);

        // when & then
        assertThatThrownBy(() -> emojiService.addEmoji(request, authentication))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMOJI_NOT_FOUND);
    }

    // TODO: 모멘트와 코멘트 작성자 아닌 사용자가 이모지를 등록하면 예외가 발생한다
}
