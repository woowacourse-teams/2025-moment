package moment.comment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
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
    void Comment_ID와_일치하는_Comment_목록을_페이징_처리하여_생성_시간_내림차순으로_원하는_커서부터_조회한다() throws InterruptedException {
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
        Thread.sleep(10);

        Comment comment2 = new Comment("moment2 comment", commenter, savedMoment2.getId());
        Comment savedComment2 = commentRepository.save(comment2);
        Thread.sleep(10);

        Comment comment3 = new Comment("moment2 comment2", commenter, savedMoment3.getId());
        Comment savedComment3 = commentRepository.save(comment3);
        Thread.sleep(10);

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
        User momentOwner = userRepository.save(
                new User("owner@user.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@user.com", "password", "commenter", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", momentOwner, WriteType.BASIC));

        Comment comment1 = commentRepository.save(new Comment("comment1", commenter, moment.getId()));
        Comment comment2 = commentRepository.save(new Comment("comment2", commenter, moment.getId()));
        commentRepository.save(
                new Comment("comment3", commenter, moment.getId())); // This one is not in the unread list

        // when
        List<Comment> result = commentRepository.findUnreadCommentsFirstPage(
                List.of(comment1.getId(), comment2.getId()),
                PageRequest.of(0, 5));

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(comment1, comment2);
    }

    @Test
    @Disabled
    void 읽지_않은_코멘트_목록의_두_번째_페이지를_조회한다() throws InterruptedException {
        // given
        User momentOwner = userRepository.save(
                new User("owner@user.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@user.com", "password", "commenter", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", momentOwner, WriteType.BASIC));

        Comment comment1 = commentRepository.save(new Comment("comment1", commenter, moment.getId()));
        Thread.sleep(10);
        Comment comment2 = commentRepository.save(new Comment("comment2", commenter, moment.getId()));
        Thread.sleep(10);
        Comment comment3 = commentRepository.save(new Comment("comment3", commenter, moment.getId()));

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

    @Test
    void 모멘트_ID_목록에_포함된_모든_코멘트를_조회한다() {
        // given
        User momentOwner = userRepository.save(
                new User("owner@user.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@user.com", "password", "commenter", ProviderType.EMAIL));

        Moment moment1 = momentRepository.save(new Moment("Moment 1", momentOwner, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("Moment 2", momentOwner, WriteType.BASIC));
        Moment moment3 = momentRepository.save(new Moment("Moment 3", momentOwner, WriteType.BASIC));

        Comment comment1 = commentRepository.save(new Comment("Comment on moment 1", commenter, moment1.getId()));
        Comment comment2 = commentRepository.save(new Comment("Comment on moment 2", commenter, moment2.getId()));
        commentRepository.save(
                new Comment("Comment on moment 3", commenter, moment3.getId()));

        List<Long> momentIdsToSearch = List.of(moment1.getId(), moment2.getId());

        // when
        List<Comment> result = commentRepository.findAllByMomentIdIn(momentIdsToSearch);

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(comment1, comment2);
    }

    @Test
    void 내가_코멘트하지_않은_모멘트_ID_목록을_조회한다() {
        // given
        User user1 = userRepository.save(
                new User("user1@test.com", "password", "user1", ProviderType.EMAIL));
        User user2 = userRepository.save(
                new User("user2@test.com", "password", "user2", ProviderType.EMAIL));
        User user3 = userRepository.save(
                new User("user3@test.com", "password", "user3", ProviderType.EMAIL));
        User momentOwner = userRepository.save(
                new User("owner@test.com", "password", "owner", ProviderType.EMAIL));

        Moment moment1 = momentRepository.save(new Moment("Moment 1", momentOwner, WriteType.BASIC));
        Moment moment2 = momentRepository.save(new Moment("Moment 2", momentOwner, WriteType.BASIC));
        Moment moment3 = momentRepository.save(new Moment("Moment 3", momentOwner, WriteType.BASIC));
        Moment moment4 = momentRepository.save(new Moment("Moment 4", momentOwner, WriteType.BASIC));

        commentRepository.save(new Comment("User1 on Moment1", user1, moment1.getId())); // 내가 코멘트

        commentRepository.save(new Comment("User2 on Moment2", user2, moment2.getId()));
        commentRepository.save(new Comment("User3 on Moment2", user3, moment2.getId()));

        commentRepository.save(new Comment("User2 on Moment3", user2, moment3.getId()));
        commentRepository.save(new Comment("User1 on Moment3", user1, moment3.getId())); // 내가 코멘트

        List<Long> momentIdsToSearch = List.of(moment1.getId(), moment2.getId(), moment3.getId(), moment4.getId());

        // when
        List<Long> result = commentRepository.findMomentIdsNotCommentedOnByMe(momentIdsToSearch, user1.getId());

        // then
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(moment2.getId(), moment4.getId());
    }

    @Test
    void 모멘트ID와_코멘터ID로_코멘트_존재함을_확인한다() {
        // given
        User momentOwner = userRepository.save(
                new User("owner@test.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@test.com", "password", "commenter", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", momentOwner, WriteType.BASIC));
        commentRepository.save(new Comment("A comment", commenter, moment.getId()));

        // when
        boolean result = commentRepository.existsByMomentIdAndCommenterId(moment.getId(), commenter.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 모멘트ID와_코멘터ID로_코멘트_존재하지_않음을_확인한다() {
        // given
        User momentOwner = userRepository.save(
                new User("owner@test.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@test.com", "password", "commenter", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", momentOwner, WriteType.BASIC));

        // when
        boolean result = commentRepository.existsByMomentIdAndCommenterId(moment.getId(), commenter.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 코멘트_ID로_모멘트_ID를_조회한다() {
        // given
        User momentOwner = userRepository.save(
                new User("owner@test.com", "password", "owner", ProviderType.EMAIL));
        User commenter = userRepository.save(
                new User("commenter@test.com", "password", "commenter", ProviderType.EMAIL));
        Moment moment = momentRepository.save(new Moment("A moment", momentOwner, WriteType.BASIC));
        Comment comment = commentRepository.save(new Comment("A comment", commenter, moment.getId()));

        // when
        Optional<Long> result = commentRepository.findMomentIdById(comment.getId());

        // then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get()).isEqualTo(moment.getId())
        );
    }

    @Test
    void 존재하지_않는_경우_코멘트_ID로_모멘트_ID를_조회한다() {
        // given
        Long nonExistentCommentId = 999L;

        // when
        Optional<Long> result = commentRepository.findMomentIdById(nonExistentCommentId);

        // then
        assertThat(result).isNotPresent();
    }
}
