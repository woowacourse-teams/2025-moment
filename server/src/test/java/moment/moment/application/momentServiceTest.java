package moment.moment.application;

import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.matching.application.MatchingService;
import moment.matching.domain.MatchingResult;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
    private EmojiRepository emojiRepository;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private MomentQueryService momentQueryService;

    @Mock
    private MatchingService matchingService;

    @Mock
    private BasicMomentCreatePolicy basicMomentCreatePolicy;

    @Mock
    private ExtraMomentCreatePolicy extraMomentCreatePolicy;

    @Test
    void 모멘트_생성에_성공한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        MomentCreateRequest request = new MomentCreateRequest(momentContent);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        Moment expect = new Moment(momentContent, momenter);
        ReflectionTestUtils.setField(expect, "id", 1L);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);
        given(matchingService.match(any(Long.class))).willReturn(MatchingResult.MATCHED);

        // when
        momentService.addMomentAndMatch(request, 1L);

        // then
        assertAll(
                () -> then(momentRepository).should(times(1)).save(any(Moment.class)),
                () -> then(matchingService).should(times(1)).match(any(Long.class))
        );
    }

    @Test
    void 모멘트_생성에_실패한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        MomentCreateRequest request = new MomentCreateRequest(momentContent);
        User momenter = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> momentService.addMomentAndMatch(request, 1L))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_ALREADY_EXIST);
    }

    @Test
    void 내가_작성한_모멘트를_생성_시간_순으로_정렬하여_페이지를_조회한다() {
        // given
        User momenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);
        User commenter = new User("curry@gmail.com", "12345", "커리", ProviderType.EMAIL);

        Moment moment = new Moment("야근 힘들어용 ㅠㅠ", momenter);
        Comment comment = new Comment("안됐네요.", commenter, moment);
        Emoji emoji = new Emoji("HEART", commenter, comment);

        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);

        given(momentRepository.findMyMomentFirstPage(any(User.class), any(Pageable.class)))
                .willReturn(List.of(moment));

        given(commentRepository.findAllByMomentIn(any(List.class)))
                .willReturn(List.of(comment));

        given(emojiRepository.findAllByCommentIn(any(List.class)))
                .willReturn(List.of(emoji));

        //when
        MyMomentPageResponse response = momentService.getMyMoments(null, 1, 1L);

        //then
        assertAll(
                () -> then(commentRepository).should(times(1)).findAllByMomentIn(any(List.class)),
                () -> then(emojiRepository).should(times(1)).findAllByCommentIn(any(List.class)),
                () -> then(momentRepository).should(times(1)).findMyMomentFirstPage(any(User.class), any(Pageable.class)),
                () -> assertThat(response.nextCursor()).isNull(),
                () -> assertThat(response.hasNextPage()).isFalse(),
                () -> assertThat(response.pageSize()).isEqualTo(1)
        );
    }

    @Test
    void 내가_받은_모멘트를_조회한다() {
        // given
        User commenter = new User("kiki@gmail.com", "1234", "kiki", ProviderType.EMAIL);
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        Moment moment = new Moment("아 행복해..", momenter);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.findTodayMatchedMomentByCommenter(any(User.class)))
                .willReturn(Optional.of(moment));

        // when
        MatchedMomentResponse response = momentService.getMatchedMoment(1L);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(moment.getId()),
                () -> assertThat(response.content()).isEqualTo(moment.getContent()),
                () -> assertThat(response.createdAt()).isEqualTo(moment.getCreatedAt())
        );
    }

    @Test
    void 내가_받은_모멘트가_존재하지_않는_경우_빈_데이터를_반환한다() {
        // given
        User commenter = new User("kiki@gmail.com", "1234", "kiki", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentQueryService.findTodayMatchedMomentByCommenter(any(User.class)))
                .willReturn(Optional.empty());

        // when
        MatchedMomentResponse response = momentService.getMatchedMoment(1L);

        // then
        assertAll(
                () -> assertThat(response.id()).isNull(),
                () -> assertThat(response.content()).isNull(),
                () -> assertThat(response.createdAt()).isNull()
        );
    }

    @Test
    void 오늘_모멘트를_작성할_수_있는_상태를_반환한다() {
        // given
        User commenter = new User("harden@gmail.com", "1234", "하든", ProviderType.EMAIL);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(basicMomentCreatePolicy.canCreate(any(User.class))).willReturn(true);

        MomentCreationStatusResponse response = new MomentCreationStatusResponse(MomentCreationStatus.ALLOWED);

        // when & then
        assertThat(momentService.canCreateMoment(1L)).isEqualTo(response);
    }

    @Test
    void 오늘_모멘트를_작성할_수_없는_상태를_반환한다() {
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
}
