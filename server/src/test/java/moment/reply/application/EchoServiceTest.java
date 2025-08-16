package moment.reply.application;


import java.util.Set;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.notification.application.SseNotificationService;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.domain.Echo;
import moment.reply.dto.request.EchoCreateRequest;
import moment.reply.infrastructure.EchoRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class EchoServiceTest {

    @InjectMocks
    private EchoService echoService;

    @Mock
    private EchoRepository echoRepository;

    @Mock
    private CommentQueryService commentQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private EchoQueryService echoQueryService;

    @Mock
    private SseNotificationService SseNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RewardService rewardService;

    @Test
    void 코멘트에_에코를_추가_할_수_있다() {
        // given
        Authentication authentication = new Authentication(1L);
        EchoCreateRequest request = new EchoCreateRequest(Set.of("HEART"), 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Echo echo = new Echo("HEART", momenter, comment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);
        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);
        given(echoRepository.save(any(Echo.class)))
                .willReturn(echo);
        doNothing().when(rewardService).rewardForEcho(commenter, Reason.ECHO_RECEIVED, comment.getId());

        // when
        echoService.addEchos(request, authentication);

        // then
        then(echoRepository).should(times(1)).save(any(Echo.class));
    }

    @Test
    void 모멘트와_작성자_아닌_사용자가_에코를_등록하면_예외가_발생한다() {
        // given
        Authentication authentication = new Authentication(1L);
        EchoCreateRequest request = new EchoCreateRequest(Set.of("HEART"), 1L);

        User unAuthorized = new User("noUser@gmail.com", "1234", "noUser", ProviderType.EMAIL);
        Comment comment = mock(Comment.class);
        Moment moment = mock(Moment.class);
        given(comment.getMoment()).willReturn(moment);

        given(commentQueryService.getCommentById(any(Long.class))).willReturn(comment);
        given(userQueryService.getUserById(any(Long.class))).willReturn(unAuthorized);
        given(moment.checkMomenter(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> echoService.addEchos(request, authentication))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }

    @Test
    void 코멘트의_모든_에코를_조회한다() {
        // given
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);

        // when
        echoService.getEchosByCommentId(1L);

        // then
        then(echoQueryService).should(times(1)).getEmojisByComment(any(Comment.class));
    }
}
