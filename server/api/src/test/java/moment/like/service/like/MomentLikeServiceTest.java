package moment.like.service.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.util.Optional;
import moment.fixture.GroupFixture;
import moment.fixture.GroupMemberFixture;
import moment.fixture.MomentFixture;
import moment.fixture.MomentLikeFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.like.domain.MomentLike;
import moment.like.dto.event.MomentLikeEvent;
import moment.like.infrastructure.MomentLikeRepository;
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
class MomentLikeServiceTest {

    @Mock
    private MomentLikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MomentLikeService likeService;

    @Test
    void 좋아요_토글_새로_생성() {
        // Given
        User momenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, momenter);
        GroupMember member = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, momenter, group,
            GroupMemberFixture.createOwnerMember(group, momenter, "모멘터"));

        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean isLiked = likeService.toggle(moment, member);

        // Then
        assertThat(isLiked).isTrue();
        verify(likeRepository).save(any(MomentLike.class));
    }

    @Test
    void 좋아요_토글_취소() {
        // Given
        User momenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, momenter);
        GroupMember member = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, momenter, group,
            GroupMemberFixture.createOwnerMember(group, momenter, "모멘터"));
        MomentLike existingLike = MomentLikeFixture.createMomentLike(moment, member);

        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(existingLike));

        // When
        boolean isLiked = likeService.toggle(moment, member);

        // Then
        assertThat(isLiked).isFalse();
        assertThat(existingLike.isDeleted()).isTrue();
    }

    @Test
    void 좋아요_토글_재활성화() {
        // Given
        User momenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, momenter);
        GroupMember member = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, momenter, group,
            GroupMemberFixture.createOwnerMember(group, momenter, "모멘터"));
        MomentLike deletedLike = MomentLikeFixture.createDeletedMomentLike(moment, member);

        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(deletedLike));

        // When
        boolean isLiked = likeService.toggle(moment, member);

        // Then
        assertThat(isLiked).isTrue();
        assertThat(deletedLike.isDeleted()).isFalse();
    }

    @Test
    void 좋아요_시_이벤트_발행() {
        // Given
        User momenter = UserFixture.createUserWithId(1L);
        User liker = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, momenter);
        GroupMember likerMember = GroupMemberFixture.createApprovedMember(group, liker, "좋아요유저");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, momenter, group,
            GroupMemberFixture.createOwnerMember(group, momenter, "모멘터"));

        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(moment, likerMember);

        // Then
        ArgumentCaptor<MomentLikeEvent> captor = ArgumentCaptor.forClass(MomentLikeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        MomentLikeEvent event = captor.getValue();
        assertThat(event.momentId()).isEqualTo(1L);
        assertThat(event.momentOwnerId()).isEqualTo(1L);
        assertThat(event.groupId()).isEqualTo(1L);
    }

    @Test
    void 자기_글_좋아요_시_이벤트_미발행() {
        // Given
        User momenter = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, momenter);
        GroupMember momenterMember = GroupMemberFixture.createOwnerMember(group, momenter, "모멘터");
        Moment moment = MomentFixture.createMomentInGroupWithId(1L, momenter, group, momenterMember);

        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(moment, momenterMember);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 좋아요_수_조회() {
        // Given
        when(likeRepository.countByMomentId(1L)).thenReturn(10L);

        // When
        long count = likeService.getCount(1L);

        // Then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    void 좋아요_여부_확인_좋아요함() {
        // Given
        when(likeRepository.existsByMomentIdAndMemberId(1L, 1L)).thenReturn(true);

        // When
        boolean hasLiked = likeService.hasLiked(1L, 1L);

        // Then
        assertThat(hasLiked).isTrue();
    }

    @Test
    void 좋아요_여부_확인_좋아요안함() {
        // Given
        when(likeRepository.existsByMomentIdAndMemberId(1L, 1L)).thenReturn(false);

        // When
        boolean hasLiked = likeService.hasLiked(1L, 1L);

        // Then
        assertThat(hasLiked).isFalse();
    }
}
