package moment.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.util.Optional;
import moment.comment.domain.Comment;
import moment.fixture.CommentFixture;
import moment.fixture.CommentLikeFixture;
import moment.fixture.GroupFixture;
import moment.fixture.GroupMemberFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.like.domain.CommentLike;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.infrastructure.CommentLikeRepository;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentLikeServiceTest {

    @Mock
    private CommentLikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentLikeService likeService;

    @Test
    void 좋아요_토글_새로_생성() {
        // Given
        User commenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, commenter);
        GroupMember likerMember = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, commenter, group,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        Comment comment = CommentFixture.createCommentInGroupWithId(1L, moment, commenter,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));

        when(likeRepository.findByCommentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean isLiked = likeService.toggle(comment, likerMember);

        // Then
        assertThat(isLiked).isTrue();
        verify(likeRepository).save(any(CommentLike.class));
    }

    @Test
    void 좋아요_토글_취소() {
        // Given
        User commenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, commenter);
        GroupMember likerMember = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, commenter, group,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        Comment comment = CommentFixture.createCommentInGroupWithId(1L, moment, commenter,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        CommentLike existingLike = CommentLikeFixture.createCommentLike(comment, likerMember);

        when(likeRepository.findByCommentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(existingLike));

        // When
        boolean isLiked = likeService.toggle(comment, likerMember);

        // Then
        assertThat(isLiked).isFalse();
        assertThat(existingLike.isDeleted()).isTrue();
    }

    @Test
    void 좋아요_토글_재활성화() {
        // Given
        User commenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, commenter);
        GroupMember likerMember = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, commenter, group,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        Comment comment = CommentFixture.createCommentInGroupWithId(1L, moment, commenter,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        CommentLike deletedLike = CommentLikeFixture.createDeletedCommentLike(comment, likerMember);

        when(likeRepository.findByCommentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(deletedLike));

        // When
        boolean isLiked = likeService.toggle(comment, likerMember);

        // Then
        assertThat(isLiked).isTrue();
        assertThat(deletedLike.isDeleted()).isFalse();
    }

    @Test
    void 좋아요_시_이벤트_발행() {
        // Given
        User commenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, commenter);
        GroupMember likerMember = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, commenter, group,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));
        Comment comment = CommentFixture.createCommentInGroupWithId(1L, moment, commenter,
            GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자"));

        when(likeRepository.findByCommentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(comment, likerMember);

        // Then
        ArgumentCaptor<CommentLikeEvent> captor = ArgumentCaptor.forClass(CommentLikeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        CommentLikeEvent event = captor.getValue();
        assertThat(event.commentId()).isEqualTo(1L);
        assertThat(event.commentOwnerId()).isEqualTo(1L);
        assertThat(event.groupId()).isEqualTo(1L);
    }

    @Test
    void 자기_댓글_좋아요_시_이벤트_미발행() {
        // Given
        User commenter = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, commenter);
        GroupMember commenterMember = GroupMemberFixture.createOwnerMember(group, commenter, "댓글작성자");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, commenter, group, commenterMember);
        Comment comment = CommentFixture.createCommentInGroupWithId(1L, moment, commenter, commenterMember);

        when(likeRepository.findByCommentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(comment, commenterMember);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 좋아요_수_조회() {
        // Given
        when(likeRepository.countByCommentId(1L)).thenReturn(5L);

        // When
        long count = likeService.getCount(1L);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void 좋아요_여부_확인_좋아요함() {
        // Given
        when(likeRepository.existsByCommentIdAndMemberId(1L, 1L)).thenReturn(true);

        // When
        boolean hasLiked = likeService.hasLiked(1L, 1L);

        // Then
        assertThat(hasLiked).isTrue();
    }

    @Test
    void 좋아요_여부_확인_좋아요안함() {
        // Given
        when(likeRepository.existsByCommentIdAndMemberId(1L, 1L)).thenReturn(false);

        // When
        boolean hasLiked = likeService.hasLiked(1L, 1L);

        // Then
        assertThat(hasLiked).isFalse();
    }
}
