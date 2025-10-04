package moment.reply.application;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import moment.comment.service.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.notification.service.NotificationFacade;
import moment.comment.domain.Echo;
import moment.reply.dto.request.EchoCreateRequest;
import moment.comment.infrastructure.EchoRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private NotificationFacade notificationFacade;

    @Mock
    private RewardService rewardService;

    @Test
    void 코멘트에_에코를_추가_할_수_있다() {
        // given
        Authentication authentication = new Authentication(1L);
        EchoCreateRequest request = new EchoCreateRequest(Set.of("THANKS"), 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);
        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);
        given(echoRepository.findByCommentAndUserAndEchoTypeIn(comment, momenter, Set.of("THANKS")))
                .willReturn(Collections.emptyList());
        doNothing().when(rewardService).rewardForEcho(commenter, Reason.ECHO_RECEIVED, comment.getId());

        // when
        echoService.addEchos(request, authentication);

        // then
        then(echoRepository).should(times(1)).saveAll(any());
    }

    @Test
    void 코멘트에_추가되지_않은_에코만_부분적으로_추가할_수_있다() {
        // given
        Authentication authentication = new Authentication(1L);
        EchoCreateRequest request = new EchoCreateRequest(Set.of("THANKS", "COMFORTED"), 1L);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Echo alreadyExistEcho = new Echo("THANKS", momenter, comment);

        given(commentQueryService.getCommentById(any(Long.class)))
                .willReturn(comment);
        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);
        given(echoRepository.findByCommentAndUserAndEchoTypeIn(any(), any(), anySet()))
                .willReturn(List.of(alreadyExistEcho));
        doNothing().when(rewardService).rewardForEcho(commenter, Reason.ECHO_RECEIVED, comment.getId());

        ArgumentCaptor<List<Echo>> echoCaptor = ArgumentCaptor.forClass(List.class);

        // when
        echoService.addEchos(request, authentication);

        // then
        verify(echoRepository).saveAll(echoCaptor.capture());
        List<Echo> savedEchos = echoCaptor.getValue();

        assertThat(savedEchos.size()).isEqualTo(1);
        assertThat(savedEchos.getFirst().getEchoType()).isEqualTo("COMFORTED");
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
        then(echoQueryService).should(times(1)).getEchosByComment(any(Comment.class));
    }
}
