package moment.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.request.CommentReportCreateRequest;
import moment.comment.dto.response.CommentReportCreateResponse;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentImageService;
import moment.moment.application.MomentQueryService;
import moment.moment.application.MomentTagService;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.notification.application.NotificationQueryService;
import moment.notification.application.SseNotificationService;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.application.EchoQueryService;
import moment.reply.application.EchoService;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private MomentQueryService momentQueryService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private EchoQueryService echoQueryService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentQueryService commentQueryService;

    @Mock
    private SseNotificationService SseNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RewardService rewardService;

    @Mock
    private CommentImageService commentImageService;

    @Mock
    private NotificationQueryService notificationQueryService;

    @Mock
    private MomentTagService momentTagService;

    @Mock
    private MomentImageService momentImageService;

    @Mock
    private ReportService reportService;

    @Mock
    private EchoService echoService;

    @Test
    void Comment를_등록한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        Notification notification = new Notification(
                momenter,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        doNothing().when(rewardService).rewardForComment(commenter, Reason.COMMENT_CREATION, comment.getId());

        // when
        commentService.addComment(request, 1L);

        // then
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    void 존재하지_않는_Moment에_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);

        given(userQueryService.getUserById(any(Long.class))).willReturn(
                new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL));
        given(momentQueryService.getMomentById(any(Long.class))).willThrow(
                new MomentException(ErrorCode.MOMENT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_User가_Comment_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);

        given(userQueryService.getUserById(any(Long.class))).willThrow(new MomentException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void Commenter가_일치하는_Comment_목록을_생성_시간_내림차순으로_페이지_사이즈만큼_페이징_처리하여_불러온다() {
        // given
        User momenter1 = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        User momenter2 = new User("drago@gmail.com", "1234", "drago", ProviderType.EMAIL);
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);

        Moment moment1 = new Moment("오늘 하루는 맛있는 하루~", true, momenter1, WriteType.BASIC);
        Moment moment2 = new Moment("오늘 하루는 행복한 하루~", true, momenter2, WriteType.BASIC);

        Comment comment1 = new Comment("moment1 comment", commenter, moment1);
        LocalDateTime now1 = LocalDateTime.now();
        ReflectionTestUtils.setField(comment1, "id", 1L);
        ReflectionTestUtils.setField(comment1, "createdAt", now1);

        Comment comment2 = new Comment("moment2 comment", commenter, moment2);
        LocalDateTime now2 = LocalDateTime.now();
        ReflectionTestUtils.setField(comment2, "id", 2L);
        ReflectionTestUtils.setField(comment2, "createdAt", now2);

        // given

        List<Comment> expectedComments = List.of(comment2, comment1);

        given(commentRepository.findFirstPageCommentIdsByCommenter(any(), any())).willReturn(List.of());
        given(commentRepository.findCommentsWithDetailsByIds(any(List.class))).willReturn(expectedComments);
        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(echoQueryService.getAllByCommentIn(any(List.class))).willReturn(Collections.emptyList());

        // when
        MyCommentPageResponse actualComments = commentService.getCommentsByUserIdWithCursor(null, 1, 1L);

        // then
        assertAll(
                () -> assertThat(actualComments.items().myCommentsResponse()).hasSize(1),
                () -> assertThat(actualComments.nextCursor()).isEqualTo(String.format("%s_%s", now2, 2)),
                () -> assertThat(actualComments.hasNextPage()).isTrue(),
                () -> assertThat(actualComments.pageSize()).isEqualTo(1)
        );
    }

    @Test
    void 존재하지_않는_Commenter가_Comment_목록을_조회하는_경우_예외가_발생한다() {
        // given
        given(userQueryService.getUserById(any(Long.class))).willThrow(new MomentException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsByUserIdWithCursor(null, 2, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 동일_유저가_이미_코멘트를_등록한_모멘트에_다시_등록하는_경우_예외가_발생한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentQueryService.existsByMomentAndCommenter(moment, commenter)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> commentService.addComment(request, 1L))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_CONFLICT);
    }

    @Test
    void 다른_유저가_이미_코멘트가_있는_모멘트에_새로_코멘트를_등록하는_경우_성공한다() {
        // given
        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);
        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        ReflectionTestUtils.setField(commenter, "id", 1L);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment newComment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        ReflectionTestUtils.setField(newComment, "id", 1L);

        Notification notification = new Notification(
                momenter,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentQueryService.existsByMomentAndCommenter(moment, commenter)).willReturn(false);
        given(commentRepository.save(any(Comment.class))).willReturn(newComment);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        doNothing().when(rewardService).rewardForComment(any(), any(), any());

        // when & then
        assertThatCode(() -> commentService.addComment(request, commenter.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미지를_포함한_Comment를_등록한다() {
        // given
        String imageUrl = "https://asdfasdfasdfasdfasdfasdfcat.jgp";
        String imageName = "cat.jpg";

        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, imageUrl, imageName);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);

        CommentImage commentImage = new CommentImage(comment, imageUrl, imageName);

        Notification notification = new Notification(
                momenter,
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                1L);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.getMomentById(any(Long.class))).willReturn(moment);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(commentImageService.create(any(CommentCreateRequest.class), any(Comment.class)))
                .willReturn(Optional.of(commentImage));
        doNothing().when(rewardService).rewardForComment(commenter, Reason.COMMENT_CREATION, comment.getId());

        // when
        commentService.addComment(request, 1L);

        // then
        then(commentRepository).should(times(1)).save(any(Comment.class));
    }

    @Test
    void 읽지_않은_코멘트를_조회한다() {
        // given
        Long commenterId = 1L;
        User commenter = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        Moment moment = new Moment("안녕", commenter, WriteType.BASIC);
        Comment comment1 = new Comment("안녕1", commenter, moment);
        ReflectionTestUtils.setField(comment1, "id", 1L);
        Comment comment2 = new Comment("안녕2", commenter, moment);
        ReflectionTestUtils.setField(comment2, "id", 2L);

        Notification notification1 = new Notification(commenter, null, TargetType.COMMENT, 1L);
        Notification notification2 = new Notification(commenter, null, TargetType.COMMENT, 2L);

        given(userQueryService.getUserById(commenterId)).willReturn(commenter);
        given(notificationQueryService.getUnreadContentsNotifications(commenter, TargetType.COMMENT))
                .willReturn(List.of(notification1, notification2));
        given(commentRepository.findUnreadCommentsFirstPage(any(), any()))
                .willReturn(List.of(comment1, comment2));
        given(echoQueryService.getAllByCommentIn(any())).willReturn(Collections.emptyList());

        // when
        MyCommentPageResponse response = commentService.getMyUnreadComments(null, 10, commenterId);

        // then
        assertAll(
                () -> assertThat(response.items().myCommentsResponse()).hasSize(2),
                () -> assertThat(response.hasNextPage()).isFalse(),
                () -> then(notificationQueryService).should(times(1))
                        .getUnreadContentsNotifications(commenter, TargetType.COMMENT),
                () -> then(commentRepository).should(times(1)).findUnreadCommentsFirstPage(any(), any())
        );
    }

    @Test
    void 코멘트를_신고한다() {
        // given
        Long momenterId = 2L;

        Long commentId = 4L;

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        ReflectionTestUtils.setField(momenter, "id", momenterId);

        Moment moment = new Moment("잘자요", momenter, WriteType.BASIC);
        Comment comment = new Comment("정말 안타깝게 됐네요!", commenter, moment);
        ReflectionTestUtils.setField(comment, "id", commentId);

        Report report = new Report(momenter, TargetType.COMMENT, commentId, ReportReason.SEXUAL_CONTENT);

        CommentReportCreateRequest request = new CommentReportCreateRequest("SEXUAL_CONTENT");

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(commentQueryService.getCommentWithCommenterById(any(Long.class))).willReturn(comment);
        given(reportService.createReport(TargetType.COMMENT, momenter, comment.getId(), request.reason()))
                .willReturn(report);

        // when
        CommentReportCreateResponse commentReportCreateResponse = commentService.reportComment(
                comment.getId(),
                momenter.getId(),
                request
        );

        // then
        then(commentRepository).should(times(1))
                .delete(any());
        then(echoService).should(times(1))
                .deleteByComment(any());
        then(commentImageService).should(times(1))
                .deleteByComment(any());
        then(reportService).should(times(1))
                .createReport(TargetType.COMMENT, momenter, comment.getId(), request.reason());
    }
}
