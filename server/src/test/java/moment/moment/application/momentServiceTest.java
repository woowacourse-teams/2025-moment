package moment.moment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.matching.application.MatchingService;
import moment.matching.domain.MatchingResult;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
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
    private MatchingService matchingService;

    @Test
    void 모멘트_생성에_성공한다() {
        // given
        String momentContent = "재미있는 내용이네요.";
        MomentCreateRequest request = new MomentCreateRequest(momentContent);
        User momenter = new User("lebron@gmail.com", "1234", "르브론");
        Moment expect = new Moment(momentContent, momenter);
        ReflectionTestUtils.setField(expect, "id", 1L);

        given(momentRepository.save(any(Moment.class))).willReturn(expect);
        given(userQueryService.getUserById(any(Long.class))).willReturn(momenter);
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
    void 내가_작성한_모멘트를_조회한다() {
        // given
        User momenter = new User("harden@gmail.com", "1234", "하든");
        User commenter = new User("curry@gmail.com", "12345", "커리");

        Moment moment = new Moment("야근 힘들어용 ㅠㅠ", momenter);
        Comment comment = new Comment("안됐네요.", commenter, moment);
        Emoji emoji = new Emoji(EmojiType.HEART, commenter, comment);

        given(userQueryService.getUserById(any(Long.class)))
                .willReturn(momenter);
        
        given(momentRepository.findMomentByMomenter(any(User.class)))
                .willReturn(List.of(moment));

        given(commentRepository.findAllByMomentIn(any(List.class)))
                .willReturn(List.of(comment));

        given(emojiRepository.findAllByCommentIn(any(List.class)))
                .willReturn(List.of(emoji));

        //when
        List<MyMomentResponse> myMomentResponses = momentService.getMyMoments(1L);
        System.out.println(myMomentResponses);

        //then
        assertAll(
                () -> then(commentRepository).should(times(1)).findAllByMomentIn(any(List.class)),
                () -> then(emojiRepository).should(times(1)).findAllByCommentIn(any(List.class)),
                () -> then(momentRepository).should(times(1)).findMomentByMomenter(any(User.class))
        );
    }

    @Test
    void 내가_받은_모멘트를_조회한다() {
        // given
        User commenter = new User("kiki@gmail.com", "1234", "kiki");
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        Moment moment = new Moment("아 행복해..", momenter);

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentRepository.findMatchedMomentByCommenter(
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .willReturn(Optional.of(moment));
        // when
        MatchedMomentResponse response = momentService.getMatchedMoment(1L);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(moment.getId()),
                () -> assertThat(response.content()).isEqualTo(moment.getContent()),
                () -> assertThat(response.createdAt()).isEqualTo(moment.getCreatedAt()),
                () -> then(momentRepository).should(times(1)).findMatchedMomentByCommenter(
                        any(User.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class))
        );
    }

    @Test
    void 내가_받은_모멘트가_존재하지_않는_경우_빈_데이터를_반환한다() {
        // given
        User commenter = new User("kiki@gmail.com", "1234", "kiki");

        given(userQueryService.getUserById(any(Long.class))).willReturn(commenter);
        given(momentRepository.findMatchedMomentByCommenter(
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .willReturn(Optional.empty());
        // when
        MatchedMomentResponse response = momentService.getMatchedMoment(1L);

        // then
        assertAll(
                () -> assertThat(response.id()).isNull(),
                () -> assertThat(response.content()).isNull(),
                () -> assertThat(response.createdAt()).isNull(),
                () -> then(momentRepository).should(times(1)).findMatchedMomentByCommenter(
                        any(User.class),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class))
        );
    }
}
