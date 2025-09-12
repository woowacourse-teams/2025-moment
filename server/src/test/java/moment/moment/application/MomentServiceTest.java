package moment.moment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moment.comment.application.CommentImageService;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.application.NotificationQueryService;
import moment.notification.domain.Notification;
import moment.reply.application.EchoQueryService;
import moment.reply.domain.Echo;
import moment.report.application.ReportService;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentServiceTest {

    @InjectMocks
    private MomentService momentService;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentQueryService commentQueryService;

    @Mock
    private EchoQueryService echoQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private BasicMomentCreatePolicy basicMomentCreatePolicy;

    @Mock
    private ExtraMomentCreatePolicy extraMomentCreatePolicy;

    @Mock
    private RewardService rewardService;

    @Mock
    private MomentImageService momentImageService;

    @Mock
    private TagService tagService;

    @Mock
    private MomentTagService momentTagService;

    @Mock
    private MomentTagQueryService momentTagQueryService;

    @Mock
    private NotificationQueryService notificationQueryService;

    @Mock
    private CommentImageService commentImageService;

    @Mock
    private MomentQueryService momentQueryService;

    @Mock
    private ReportService reportService;

    @Test
    void 기본_모멘트_생성에_성공한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        List<String> tagNames = List.of("일상/여가");
        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, null, null);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(expect, "id", 1L);
        Tag tag = new Tag("일상/여가");
        MomentTag momentTag = new MomentTag(expect, tag);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        doNothing().when(rewardService).rewardForMoment(momenter, Reason.MOMENT_CREATION, expect.getId());
        given(tagService.getOrRegister(any(String.class))).willReturn(tag);
        given(momentTagService.save(any(Moment.class), any(Tag.class))).willReturn(momentTag);

        ArgumentCaptor<Moment> captor = ArgumentCaptor.forClass(Moment.class);

        // when
        momentService.addBasicMoment(request, 1L);

        // then
        verify(momentRepository).save(captor.capture());
        Moment savedMoment = captor.getValue();
        assertAll(
                () -> assertThat(savedMoment.getWriteType()).isEqualTo(WriteType.BASIC),
                () -> then(momentRepository).should(times(1)).save(any(Moment.class))
        );
    }

    @Test
    void 기본_모멘트_생성에_실패한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        List<String> tagNames = List.of("일상/여가");
        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, null, null);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> momentService.addBasicMoment(request, 1L))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_ALREADY_EXIST);
    }

    @Test
    void 추가_모멘트_생성에_성공한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        List<String> tagNames = List.of("일상/여가");
        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, null, null);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(expect, "id", 1L);
        Tag tag = new Tag("일상/여가");
        MomentTag momentTag = new MomentTag(expect, tag);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(extraMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        doNothing().when(rewardService).useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, expect.getId());
        given(tagService.getOrRegister(any(String.class))).willReturn(new Tag("일상/여가"));
        given(momentTagService.save(any(Moment.class), any(Tag.class))).willReturn(momentTag);

        ArgumentCaptor<Moment> captor = ArgumentCaptor.forClass(Moment.class);

        // when
        momentService.addExtraMoment(request, 1L);

        // then
        verify(momentRepository).save(captor.capture());
        Moment savedMoment = captor.getValue();
        assertAll(
                () -> assertThat(savedMoment.getWriteType()).isEqualTo(WriteType.EXTRA),
                () -> then(momentRepository).should(times(1)).save(any(Moment.class)),
                () -> then(rewardService).should(times(1)).useReward(
                        momenter, Reason.MOMENT_ADDITIONAL_USE, expect.getId())
        );
    }

    @Test
    void 추가_모멘트_생성에_실패한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        List<String> tagNames = List.of("일상/여가");
        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, null, null);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(expect, "id", 1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(extraMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> momentService.addExtraMoment(request, 1L))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_ENOUGH_STAR);
    }

    @Test
    void 내가_작성한_모멘트를_생성_시간_순으로_정렬하여_페이지를_조회한다() {
        // given
        User momenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);
        User commenter = new User("curry@gmail.com", "12345", "커리", ProviderType.EMAIL);

        Moment moment = new Moment("야근 힘들어용 ㅠㅠ", momenter, WriteType.BASIC);
        Comment comment = new Comment("안됐네요.", commenter, moment);
        Echo echo = new Echo("HEART", commenter, comment);
        Tag tag = new Tag("일상/여가");
        MomentTag momentTag = new MomentTag(moment, tag);

        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);

        given(momentRepository.findMyMomentFirstPage(any(User.class), any(Pageable.class)))
                .willReturn(List.of(moment));

        given(commentQueryService.getAllByMomentIn(any(List.class)))
                .willReturn(List.of(comment));

        given(echoQueryService.getEchosOfComments(any(List.class)))
                .willReturn(Map.of(comment, List.of(echo)));

        // when
        MyMomentPageResponse response = momentService.getMyMoments(null, 1, 1L);

        // then
        assertAll(
                () -> then(commentQueryService).should(times(1)).getAllByMomentIn(any(List.class)),
                () -> then(echoQueryService).should(times(1)).getEchosOfComments(any(List.class)),
                () -> then(momentRepository).should(times(1))
                        .findMyMomentFirstPage(any(User.class), any(Pageable.class)),
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
        User momenter = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        Moment savedMoment = new Moment("레벨3 (리)바이", momenter, WriteType.BASIC);
        Long momentId = 1L;
        ReflectionTestUtils.setField(savedMoment, "id", momentId);
        Tag tag = new Tag("일상/여가");
        MomentTag momentTag = new MomentTag(savedMoment, tag);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        given(momentRepository.save(any(Moment.class))).willReturn(savedMoment);
        given(tagService.getOrRegister(any(String.class))).willReturn(new Tag("일상/여가"));
        given(momentTagService.save(any(Moment.class), any(Tag.class))).willReturn(momentTag);

        List<String> tagNames = List.of("일상/여가");
        MomentCreateRequest request = new MomentCreateRequest("레벨3도 끝나가네여", tagNames, null, null);

        // when
        momentService.addBasicMoment(request, 1L);

        // then
        then(rewardService).should(times(1))
                .rewardForMoment(momenter, Reason.MOMENT_CREATION, momentId);
    }

    @Test
    void 코멘트를_달_수_있는_모멘트를_반환한다() {
        // given
        Long userId = 1L;
        User user = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        Moment moment = new Moment("안녕", user, WriteType.BASIC);
        List<String> tagNames = List.of("일상/여가");

        given(userQueryService.getUserById(userId)).willReturn(user);
        given(momentRepository.findCommentableMomentsByTagNames(any(), any(), any())).willReturn(List.of(moment));

        // when
        CommentableMomentResponse response = momentService.getCommentableMoment(userId, tagNames);

        // then
        assertThat(response.id()).isEqualTo(moment.getId());
        assertThat(response.content()).isEqualTo(moment.getContent());
    }

    @Test
    void 코멘트를_달_수_있는_이미지가_첨부된_모멘트를_반환한다() {
        // given
        Long userId = 1L;
        User user = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        Moment moment = new Moment("안녕", user, WriteType.BASIC);

        String imageUrl = "https://s3:moment-dev/images/고양이.jpg";
        String imageName = "고양이.jpg";
        MomentImage momentImage = new MomentImage(moment, imageUrl, imageName);
        List<String> tagNames = List.of("일상/여가");

        given(userQueryService.getUserById(userId)).willReturn(user);
        given(momentRepository.findCommentableMomentsByTagNames(any(), any(), any())).willReturn(List.of(moment));
        given(momentImageService.findMomentImage(any(Moment.class))).willReturn(Optional.of(momentImage));

        // when
        CommentableMomentResponse response = momentService.getCommentableMoment(userId, tagNames);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(moment.getId()),
                () -> assertThat(response.content()).isEqualTo(moment.getContent()),
                () -> assertThat(response.imageUrl()).isEqualTo(momentImage.getImageUrl())
        );
    }

    @Test
    void 코멘트를_달_수_있는_모멘트가_없는_경우_빈_값을_반환한다() {
        // given
        Long userId = 1L;
        User user = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        List<String> tagNames = List.of("일상/여가");

        given(userQueryService.getUserById(userId)).willReturn(user);
        given(momentRepository.findCommentableMomentsByTagNames(any(), any(), any())).willReturn(
                Collections.emptyList());

        // when
        CommentableMomentResponse response = momentService.getCommentableMoment(userId, tagNames);

        // then
        assertThat(response).isEqualTo(CommentableMomentResponse.empty());
    }

    @Test
    void 이미지를_포함한_기본_모멘트를_작성한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = "cat.jpg";
        List<String> tagNames = List.of("일상/여가");

        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, imageUrl, imageName);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        MomentImage momentImage = new MomentImage(expect, imageUrl, imageName);

        ReflectionTestUtils.setField(expect, "id", 1L);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        given(momentImageService.create(any(MomentCreateRequest.class), any(Moment.class)))
                .willReturn(Optional.of(momentImage));
        doNothing().when(rewardService).rewardForMoment(momenter, Reason.MOMENT_CREATION, expect.getId());

        ArgumentCaptor<Moment> captor = ArgumentCaptor.forClass(Moment.class);

        // when
        momentService.addBasicMoment(request, 1L);

        // then
        verify(momentRepository).save(captor.capture());
        Moment savedMoment = captor.getValue();
        assertAll(
                () -> assertThat(savedMoment.getWriteType()).isEqualTo(WriteType.BASIC),
                () -> then(momentRepository).should(times(1)).save(any(Moment.class)),
                () -> then(momentImageService).should(times(1))
                        .create(any(MomentCreateRequest.class), any(Moment.class))
        );
    }

    @Test
    void 이미지를_포함한_추가_모멘트를_작성한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = "cat.jpg";
        List<String> tagNames = List.of("일상/여가");

        MomentCreateRequest request = new MomentCreateRequest(momentContent, tagNames, imageUrl, imageName);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter, WriteType.BASIC);
        MomentImage momentImage = new MomentImage(expect, imageUrl, imageName);

        ReflectionTestUtils.setField(expect, "id", 1L);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(extraMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        given(momentImageService.create(any(MomentCreateRequest.class), any(Moment.class)))
                .willReturn(Optional.of(momentImage));
        doNothing().when(rewardService).useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, expect.getId());

        ArgumentCaptor<Moment> captor = ArgumentCaptor.forClass(Moment.class);

        // when
        momentService.addExtraMoment(request, 1L);

        // then
        verify(momentRepository).save(captor.capture());
        Moment savedMoment = captor.getValue();
        assertAll(
                () -> assertThat(savedMoment.getWriteType()).isEqualTo(WriteType.EXTRA),
                () -> then(momentRepository).should(times(1)).save(any(Moment.class)),
                () -> then(momentImageService).should(times(1))
                        .create(any(MomentCreateRequest.class), any(Moment.class))
        );
    }

    @Test
    void 읽지_않은_모멘트를_조회한다() {
        // given
        Long momenterId = 1L;
        User momenter = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        Moment moment1 = new Moment("안녕", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment1, "id", 1L);
        Moment moment2 = new Moment("안녕2", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment2, "id", 2L);

        Notification notification1 = new Notification(momenter, null, null, 1L);
        Notification notification2 = new Notification(momenter, null, null, 2L);

        given(userQueryService.getUserById(momenterId)).willReturn(momenter);
        given(notificationQueryService.getUnreadContentsNotifications(momenter, TargetType.MOMENT))
                .willReturn(List.of(notification1, notification2));
        given(momentRepository.findMyUnreadMomentFirstPage(any(), any()))
                .willReturn(List.of(moment1, moment2));
        given(commentQueryService.getAllByMomentIn(any())).willReturn(Collections.emptyList());
        given(momentTagService.getMomentTagsByMoment(any())).willReturn(Collections.emptyMap());

        // when
        MyMomentPageResponse response = momentService.getMyUnreadMoments(null, 10, momenterId);

        // then
        assertAll(
                () -> assertThat(response.items().myMomentsResponse()).hasSize(2),
                () -> assertThat(response.hasNextPage()).isFalse(),
                () -> then(notificationQueryService).should(times(1))
                        .getUnreadContentsNotifications(momenter, TargetType.MOMENT),
                () -> then(momentRepository).should(times(1)).findMyUnreadMomentFirstPage(any(), any())
        );
    }

    @Test
    void 정해진_신고_횟수_넘긴_모멘트를_삭제한다() {
        // given
        TargetType targetType = TargetType.MOMENT;
        User user = new User("eee@gmail.com", "1234!", "아마", ProviderType.EMAIL);

        Moment moment = new Moment("내용", user, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        User reporter1 = new User("ddd@gmail.com", "1234!", "드라고", ProviderType.EMAIL);
        Report report1 = new Report(reporter1, targetType, moment.getId(), ReportReason.ABUSE_OR_HARASSMENT);

        MomentReportCreateRequest request = new MomentReportCreateRequest("ABUSE_OR_HARASSMENT");

        given(userQueryService.getUserById(any())).willReturn(reporter1);
        given(momentQueryService.getMomentWithMomenterById(any())).willReturn(moment);
        given(reportService.createReport(any(), any(), any(), any())).willReturn(report1);

        given(reportService.countReportsByTarget(any(), any())).willReturn(3L);

        // when
        momentService.reportMoment(moment.getId(), 1L, request);

        // then
        then(momentRepository).should(times(1)).delete(any());
        then(momentImageService).should(times(1)).deleteByMoment(any());
        then(momentTagService).should(times(1)).deleteByMoment(any());
    }

    @Test
    void 정해진_신고_횟수_넘지_않은_모멘트는_삭제하지_않는다() {
        // given
        TargetType targetType = TargetType.MOMENT;
        User user = new User("eee@gmail.com", "1234!", "아마", ProviderType.EMAIL);

        Moment moment = new Moment("내용", user, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        User reporter1 = new User("ddd@gmail.com", "1234!", "드라고", ProviderType.EMAIL);
        Report report1 = new Report(reporter1, targetType, moment.getId(), ReportReason.ABUSE_OR_HARASSMENT);

        MomentReportCreateRequest request = new MomentReportCreateRequest("ABUSE_OR_HARASSMENT");

        given(userQueryService.getUserById(any())).willReturn(reporter1);
        given(momentQueryService.getMomentWithMomenterById(any())).willReturn(moment);
        given(reportService.createReport(any(), any(), any(), any())).willReturn(report1);

        given(reportService.countReportsByTarget(any(), any())).willReturn(2L);

        // when
        momentService.reportMoment(moment.getId(), 1L, request);

        // then
        then(momentRepository).should(times(0)).delete(any());
    }
}
