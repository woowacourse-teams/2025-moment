package moment.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Disabled
    void Comment_ID와_일치하는_Comment_목록을_페이징_처리하여_생성_시간_내림차순으로_조회한다() {
        // given
        User momenter1 = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        userRepository.save(momenter1);

        User momenter2 = new User("ama@gmail.com", "1234", "ama", ProviderType.EMAIL);
        userRepository.save(momenter2);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedCommenter = userRepository.save(commenter);

        Moment moment1 = new Moment("오늘 하루는 행복한 하루~", true, momenter1, WriteType.BASIC);
        Moment savedMoment1 = momentRepository.save(moment1);

        Moment moment2 = new Moment("오늘 하루는 맛있는 하루~", true, momenter2, WriteType.BASIC);
        Moment savedMoment2 = momentRepository.save(moment2);

        Comment comment1 = new Comment("moment1 comment", commenter, savedMoment1.getId());
        Comment savedComment1 = commentRepository.save(comment1);

        Comment comment2 = new Comment("moment2 comment", commenter, savedMoment2.getId());
        Comment savedComment2 = commentRepository.save(comment2);

        // when
        List<Long> commentsIds = commentRepository.findFirstPageCommentIdsByCommenter(savedCommenter,
                PageRequest.of(0, 2));
        List<Comment> comments = commentRepository.findCommentsByIds(commentsIds);

        // then
        assertAll(
                () -> assertThat(comments).hasSize(2),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment2),
                () -> assertThat(comments.getLast()).isEqualTo(savedComment1),
                () -> assertThat(comments.getFirst().getMomentId()).isEqualTo(savedMoment2.getId()),
                () -> assertThat(comments.getLast().getMomentId()).isEqualTo(savedMoment1.getId())
        );
    }


    @Test
    @Disabled
    void Comment_ID와_일치하는_Comment_목록을_페이징_처리하여_생성_시간_내림차순으로_원하는_커서부터_조회한다() {
        // given
        User momenter1 = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        userRepository.save(momenter1);

        User momenter2 = new User("ama@gmail.com", "1234", "ama", ProviderType.EMAIL);
        userRepository.save(momenter2);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedCommenter = userRepository.save(commenter);

        Moment moment1 = new Moment("오늘 하루는 행복한 하루~", true, momenter1, WriteType.BASIC);
        Moment savedMoment1 = momentRepository.save(moment1);

        Moment moment2 = new Moment("오늘 하루는 맛있는 하루~", true, momenter2, WriteType.BASIC);
        Moment savedMoment2 = momentRepository.save(moment2);

        Moment moment3 = new Moment("오늘 하루는 맛있는 하루~", true, momenter1, WriteType.BASIC);
        Moment savedMoment3 = momentRepository.save(moment3);

        Moment moment4 = new Moment("오늘 하루는 맛있는 하루~", true, momenter2, WriteType.BASIC);
        Moment savedMoment4 = momentRepository.save(moment4);

        Comment comment1 = new Comment("moment1 comment", commenter, savedMoment1.getId());
        Comment savedComment1 = commentRepository.save(comment1);

        Comment comment2 = new Comment("moment2 comment", commenter, savedMoment2.getId());
        Comment savedComment2 = commentRepository.save(comment2);

        Comment comment3 = new Comment("moment2 comment2", commenter, savedMoment3.getId());
        Comment savedComment3 = commentRepository.save(comment3);

        Comment comment4 = new Comment("moment2 comment3", commenter, savedMoment4.getId());
        Comment savedComment4 = commentRepository.save(comment4);

        // when
        List<Long> commentIds = commentRepository.findNextPageCommentIdsByCommenter(savedCommenter,
                savedComment4.getCreatedAt(),
                savedComment4.getId(),
                PageRequest.of(0, 3));
        
        List<Comment> comments = commentRepository.findCommentsByIds(commentIds);

        // then
        assertAll(
                () -> assertThat(comments).hasSize(3),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment3),
                () -> assertThat(comments.get(1)).isEqualTo(savedComment2),
                () -> assertThat(comments.getLast()).isEqualTo(savedComment1)
        );
    }

    @Test
    void 읽지_않은_코멘트_목록의_첫_페이지를_조회한다() {
        // given
        User user = userRepository.save(new User("test@user.com", "password", "tester", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", user, WriteType.BASIC));
        Comment comment1 = commentRepository.save(new Comment("comment1", user, moment.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment2", user, moment.getId()));
        commentRepository.save(new Comment("comment3", user, moment.getId())); // This one is not unread

        // when
        List<Comment> result = commentRepository.findUnreadCommentsFirstPage(List.of(comment1.getId(), comment2.getId()),
                PageRequest.of(0, 5));

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(comment1, comment2);
    }

    @Disabled
    @Test
    void 읽지_않은_코멘트_목록의_두_번째_페이지를_조회한다() throws InterruptedException {
        // given
        User user = userRepository.save(new User("test@user.com", "password", "tester", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", user, WriteType.BASIC));
        Comment comment1 = commentRepository.save(new Comment("comment1", user, moment.getId()));
        Thread.sleep(10);
        Comment comment2 = commentRepository.save(new Comment("comment2", user, moment.getId()));
        Thread.sleep(10);
        Comment comment3 = commentRepository.save(new Comment("comment3", user, moment.getId()));

        // when
        List<Comment> result = commentRepository.findUnreadCommentsNextPage(
                List.of(comment1.getId(), comment2.getId(), comment3.getId()),
                comment3.getCreatedAt(),
                comment3.getId(),
                PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1)
                .containsExactly(comment2);
    }


}
