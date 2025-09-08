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

        Comment comment1 = new Comment("moment1 comment", commenter, moment1);
        Comment savedComment1 = commentRepository.save(comment1);

        Comment comment2 = new Comment("moment2 comment", commenter, moment2);
        Comment savedComment2 = commentRepository.save(comment2);

        // when
        List<Comment> comments = commentRepository.findCommentsFirstPage(savedCommenter, PageRequest.of(0, 2));
        // then
        assertAll(
                () -> assertThat(comments).hasSize(2),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment2),
                () -> assertThat(comments.getLast()).isEqualTo(savedComment1),
                () -> assertThat(comments.getFirst().getMoment()).isEqualTo(savedMoment2),
                () -> assertThat(comments.getLast().getMoment()).isEqualTo(savedMoment1)
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

        Comment comment1 = new Comment("moment1 comment", commenter, savedMoment1);
        Comment savedComment1 = commentRepository.save(comment1);

        Comment comment2 = new Comment("moment2 comment", commenter, savedMoment2);
        Comment savedComment2 = commentRepository.save(comment2);

        Comment comment3 = new Comment("moment2 comment2", commenter, savedMoment3);
        Comment savedComment3 = commentRepository.save(comment3);

        Comment comment4 = new Comment("moment2 comment3", commenter, savedMoment4);
        Comment savedComment4 = commentRepository.save(comment4);

        // when
        List<Comment> comments = commentRepository.findCommentsNextPage(savedCommenter,
                savedComment4.getCreatedAt(),
                savedComment4.getId(),
                PageRequest.of(0, 3));

        // then
        assertAll(
                () -> assertThat(comments).hasSize(3),
                () -> assertThat(comments.getFirst()).isEqualTo(savedComment3),
                () -> assertThat(comments.get(1)).isEqualTo(savedComment2),
                () -> assertThat(comments.getLast()).isEqualTo(savedComment1)
        );
    }

    @Test
    void User가_Moment에_작성한_Comment가_존재하면_true를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        userRepository.save(commenter);

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        momentRepository.save(moment);

        Comment comment = new Comment("첫 번째 댓글", commenter, moment);
        commentRepository.save(comment);

        // when & then
        assertThat(commentRepository.existsByMomentAndCommenter(moment, commenter)).isTrue();
    }

    @Test
    void User가_Moment에_작성한_Comment가_존재하지_않으면_false를_반환한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        userRepository.save(commenter);

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, momenter, WriteType.BASIC);
        momentRepository.save(moment);

        // when & then
        assertThat(commentRepository.existsByMomentAndCommenter(moment, commenter)).isFalse();
    }

    @Test
    void 특정_기간_동안_등록된_moment에_대한_comment_개수를_센다() throws InterruptedException {
        // given
        User user = userRepository.save(new User("test@user.com", "password", "tester", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", user, WriteType.BASIC));

        LocalDateTime startTime = LocalDateTime.now();

        Thread.sleep(10);
        commentRepository.save(new Comment("In range 1", user, moment));
        Thread.sleep(10);
        commentRepository.save(new Comment("In range 2", user, moment));
        Thread.sleep(10);
        commentRepository.save(new Comment("Too new", user, moment));

        // when
        LocalDateTime endTime = LocalDateTime.now();
        long count = commentRepository.countByMomentAndCreatedAtBetween(moment, startTime, endTime);

        // then
        assertThat(count).isEqualTo(3);
    }
}
