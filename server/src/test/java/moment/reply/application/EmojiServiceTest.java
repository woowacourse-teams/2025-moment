package moment.reply.application;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.notification.application.NotificationService;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.domain.Emoji;
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

    @Mock
    private EmojiQueryService emojiQueryService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

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
        Emoji emoji = new Emoji("HEART", momenter, comment);

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
    void 모멘트와_작성자_아닌_사용자가_이모지를_등록하면_예외가_발생한다() {
        // given
        Authentication authentication = new Authentication(1L);
        EmojiCreateRequest request = new EmojiCreateRequest("HEART", 1L);

        User unAuthorized = new User("noUser@gmail.com", "1234", "noUser");
        Comment comment = mock(Comment.class);
        Moment moment = mock(Moment.class);
        given(comment.getMoment()).willReturn(moment);

        given(commentQueryService.getCommentById(any(Long.class))).willReturn(comment);
        given(userQueryService.getUserById(any(Long.class))).willReturn(unAuthorized);
        given(moment.checkMomenter(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> emojiService.addEmoji(request, authentication))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }

    @Test
    void 코멘트의_모든_이모지를_조회한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);

        // when
        emojiService.getEmojisByCommentId(1L);

        // then
        then(emojiQueryService).should(times(1)).getEmojisByComment(any(Comment.class));
    }

    @Test
    void 이모지를_제거한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Emoji emoji = new Emoji("HEART", momenter, comment);

        given(emojiQueryService.getEmojiById(any(Long.class)))
                .willReturn(emoji);
        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);

        // when
        emojiService.removeEmojiById(1L, 1L);

        // then
        then(emojiRepository).should(times(1)).delete(any(Emoji.class));
    }

    @Test
    void 이모지_작성자가_아닌_회원이_삭제요청_할_경우_예외가_발생한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Emoji emoji = new Emoji("HEART", momenter, comment);

        given(emojiQueryService.getEmojiById(any(Long.class)))
                .willReturn(emoji);

        // when & then
        assertThatThrownBy(() -> emojiService.removeEmojiById(1L, comment.getId()))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }
}
