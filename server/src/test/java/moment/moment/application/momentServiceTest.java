package moment.moment.application;

import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Echo;
import moment.reply.infrastructure.EchoRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class momentServiceTest {

    @InjectMocks
    private MomentService momentService;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EchoRepository echoRepository;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private BasicMomentCreatePolicy basicMomentCreatePolicy;

    @Mock
    private ExtraMomentCreatePolicy extraMomentCreatePolicy;

    @Mock
    private RewardService rewardService;

    @Test
    void 기본_모멘트_생성에_성공한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        MomentCreateRequest request = new MomentCreateRequest(momentContent);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(expect, "id", 1L);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        doNothing().when(rewardService).rewardForMoment(momenter, Reason.MOMENT_CREATION, expect.getId());

        // when
        momentService.addBasicMoment(request, 1L);

        // then
        then(momentRepository).should(times(1)).save(any(Moment.class));
    }

    @Test
    void 기본_모멘트_생성에_실패한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        MomentCreateRequest request = new MomentCreateRequest(momentContent);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> momentService.addBasicMoment(request, 1L))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_ALREADY_EXIST);
    }

    @Test
    void 내가_작성한_모멘트를_생성_시간_순으로_정렬하여_페이지를_조회한다() {
        // given
        User momenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);
        User commenter = new User("curry@gmail.com", "12345", "커리", ProviderType.EMAIL);

        Moment moment = new Moment("야근 힘들어용 ㅠㅠ", momenter, WriteType.BASIC);
        Comment comment = new Comment("안됐네요.", commenter, moment);
        Echo echo = new Echo("HEART", commenter, comment);

        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);

        given(momentRepository.findMyMomentFirstPage(any(User.class), any(Pageable.class)))
                .willReturn(List.of(moment));

        given(commentRepository.findAllByMomentIn(any(List.class)))
                .willReturn(List.of(comment));

        given(echoRepository.findAllByCommentIn(any(List.class)))
                .willReturn(List.of(echo));

        //when
        MyMomentPageResponse response = momentService.getMyMoments(null, 1, 1L);

        //then
        assertAll(
                () -> then(commentRepository).should(times(1)).findAllByMomentIn(any(List.class)),
                () -> then(echoRepository).should(times(1)).findAllByCommentIn(any(List.class)),
                () -> then(momentRepository).should(times(1)).findMyMomentFirstPage(any(User.class), any(Pageable.class)),
                () -> assertThat(response.nextCursor()).isNull(),
                () -> assertThat(response.hasNextPage()).isFalse(),
                () -> assertThat(response.pageSize()).isEqualTo(1)
        );
    }

    @Test
    void 오늘_기본_모멘트를_작성할_수_있는_상태를_반환한다() {
        // given
        User commenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);

        MomentCreationStatusResponse response = new MomentCreationStatusResponse(MomentCreationStatus.ALLOWED);

        // when & then
        assertThat(momentService.canCreateMoment(1L)).isEqualTo(response);
    }

    @Test
    void 오늘_기본_모멘트를_작성할_수_없는_상태를_반환한다() {
        // given
        User commenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        MomentCreationStatusResponse response = new MomentCreationStatusResponse(MomentCreationStatus.DENIED);

        // when & then
        assertThat(momentService.canCreateMoment(1L)).isEqualTo(response);
    }

    @Test
    void 오늘_추가_모멘트를_작성할_수_있는_상태를_반환한다() {
        // given
        User momenter = new User("mimi@icloud.com", "mimi1234", "미미", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(extraMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);

        MomentCreationStatusResponse expect = new MomentCreationStatusResponse(MomentCreationStatus.ALLOWED);

        // when
        MomentCreationStatusResponse response = momentService.canCreateExtraMoment(1L);

        // then
        assertThat(response).isEqualTo(expect);
    }

    @Test
    void 오늘_추가_모멘트를_작성할_수_없는_상태를_반환한다() {
        // given
        User momenter = new User("mimi@icloud.com", "mimi1234", "미미", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(extraMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        MomentCreationStatusResponse expect = new MomentCreationStatusResponse(MomentCreationStatus.DENIED);

        // when
        MomentCreationStatusResponse response = momentService.canCreateExtraMoment(1L);

        // then
        assertThat(response).isEqualTo(expect);
    }

    @Test
    void 오늘_기본_모멘트를_작성한_경우_사용자의_포인트가_추가된다() {
        // given
        User momenter = new User("mimi@icloud.com", "mimi1234!", "미미",  ProviderType.EMAIL);
        Moment savedMoment = new Moment("레벨3 (리)바이", momenter, WriteType.BASIC);
        Long momentId = 1L;
        ReflectionTestUtils.setField(savedMoment, "id", momentId);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        given(momentRepository.save(any(Moment.class))).willReturn(savedMoment);


        MomentCreateRequest request = new MomentCreateRequest("레벨3도 끝나가네여");

        // when
        momentService.addBasicMoment(request, 1L);

        // then
        then(rewardService).should(times(1))
                .rewardForMoment(momenter, Reason.MOMENT_CREATION, momentId);
    }
}
