package moment.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import moment.admin.dto.response.AdminCommentListResponse;
import moment.admin.dto.response.AdminMomentListResponse;
import moment.admin.infrastructure.AdminGroupLogRepository;
import moment.admin.service.content.AdminContentService;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminContentServiceTest {

    @InjectMocks
    private AdminContentService adminContentService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AdminGroupLogRepository adminGroupLogRepository;

    private Moment createMockMoment() {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(100L);
        lenient().when(user.getEmail()).thenReturn("user@test.com");
        lenient().when(user.getNickname()).thenReturn("테스트유저");

        GroupMember member = mock(GroupMember.class);
        lenient().when(member.getId()).thenReturn(10L);
        lenient().when(member.getNickname()).thenReturn("그룹닉네임");
        lenient().when(member.getUser()).thenReturn(user);

        Moment moment = mock(Moment.class);
        lenient().when(moment.getId()).thenReturn(1L);
        lenient().when(moment.getContent()).thenReturn("테스트 모멘트");
        lenient().when(moment.getMember()).thenReturn(member);
        lenient().when(moment.getCreatedAt()).thenReturn(LocalDateTime.now());
        lenient().when(moment.getDeletedAt()).thenReturn(null);

        return moment;
    }

    private Comment createMockComment() {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(100L);
        lenient().when(user.getEmail()).thenReturn("user@test.com");
        lenient().when(user.getNickname()).thenReturn("테스트유저");

        GroupMember member = mock(GroupMember.class);
        lenient().when(member.getId()).thenReturn(10L);
        lenient().when(member.getNickname()).thenReturn("그룹닉네임");
        lenient().when(member.getUser()).thenReturn(user);

        Comment comment = mock(Comment.class);
        lenient().when(comment.getId()).thenReturn(1L);
        lenient().when(comment.getContent()).thenReturn("테스트 코멘트");
        lenient().when(comment.getMember()).thenReturn(member);
        lenient().when(comment.getCreatedAt()).thenReturn(LocalDateTime.now());
        lenient().when(comment.getDeletedAt()).thenReturn(null);

        return comment;
    }

    @Nested
    class 모멘트_목록_조회 {

        @Test
        void getMoments_그룹의_모멘트_목록_반환() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            Page<Moment> momentPage = new PageImpl<>(List.of(moment), PageRequest.of(0, 20), 1);
            given(momentRepository.findByGroupId(any(Long.class), any(Pageable.class))).willReturn(momentPage);
            given(commentRepository.countByMomentId(1L)).willReturn(5L);

            // when
            AdminMomentListResponse response = adminContentService.getMoments(1L, 0, 20);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(1),
                () -> assertThat(response.content().get(0).momentId()).isEqualTo(1L),
                () -> assertThat(response.content().get(0).content()).isEqualTo("테스트 모멘트")
            );
        }

        @Test
        void getMoments_페이지네이션_적용() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            PageRequest pageRequest = PageRequest.of(1, 10);
            Page<Moment> momentPage = new PageImpl<>(List.of(moment), pageRequest, 15);
            given(momentRepository.findByGroupId(any(Long.class), any(Pageable.class))).willReturn(momentPage);
            given(commentRepository.countByMomentId(any())).willReturn(0L);

            // when
            AdminMomentListResponse response = adminContentService.getMoments(1L, 1, 10);

            // then
            assertAll(
                () -> assertThat(response.page()).isEqualTo(momentPage.getNumber()),
                () -> assertThat(response.size()).isEqualTo(momentPage.getSize()),
                () -> assertThat(response.totalElements()).isEqualTo(momentPage.getTotalElements()),
                () -> assertThat(response.totalPages()).isEqualTo(momentPage.getTotalPages())
            );
        }

        @Test
        void getMoments_작성자_정보_포함() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            Page<Moment> momentPage = new PageImpl<>(List.of(moment), PageRequest.of(0, 20), 1);
            given(momentRepository.findByGroupId(any(Long.class), any(Pageable.class))).willReturn(momentPage);
            given(commentRepository.countByMomentId(any())).willReturn(0L);

            // when
            AdminMomentListResponse response = adminContentService.getMoments(1L, 0, 20);

            // then
            assertAll(
                () -> assertThat(response.content().get(0).author().memberId()).isEqualTo(10L),
                () -> assertThat(response.content().get(0).author().groupNickname()).isEqualTo("그룹닉네임"),
                () -> assertThat(response.content().get(0).author().userId()).isEqualTo(100L),
                () -> assertThat(response.content().get(0).author().userEmail()).isEqualTo("user@test.com")
            );
        }

        @Test
        void getMoments_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.getMoments(999L, 0, 20))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 모멘트_삭제 {

        @Test
        void deleteMoment_모멘트_SoftDelete_성공() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));

            // when
            adminContentService.deleteMoment(1L, 1L, 1L, "admin@test.com");

            // then
            verify(momentRepository).delete(moment);
        }

        @Test
        void deleteMoment_해당_코멘트_전체_삭제() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));

            // when
            adminContentService.deleteMoment(1L, 1L, 1L, "admin@test.com");

            // then
            verify(commentRepository).softDeleteByMomentId(1L);
        }

        @Test
        void deleteMoment_AdminGroupLog_기록_확인() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));

            // when
            adminContentService.deleteMoment(1L, 1L, 1L, "admin@test.com");

            // then
            verify(adminGroupLogRepository).save(any());
        }

        @Test
        void deleteMoment_이미_삭제된_모멘트_예외() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();
            given(moment.getDeletedAt()).willReturn(LocalDateTime.now());

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteMoment(1L, 1L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_MOMENT_ALREADY_DELETED);
        }

        @Test
        void deleteMoment_모멘트없으면_예외() {
            // given
            Group group = mock(Group.class);

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteMoment(1L, 999L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_MOMENT_NOT_FOUND);
        }

        @Test
        void deleteMoment_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteMoment(999L, 1L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 코멘트_목록_조회 {

        @Test
        void getComments_모멘트의_코멘트_목록_반환() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();
            Comment comment = createMockComment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));
            Page<Comment> commentPage = new PageImpl<>(List.of(comment), PageRequest.of(0, 20), 1);
            given(commentRepository.findByMomentId(any(Long.class), any(Pageable.class))).willReturn(commentPage);

            // when
            AdminCommentListResponse response = adminContentService.getComments(1L, 1L, 0, 20);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(1),
                () -> assertThat(response.content().get(0).commentId()).isEqualTo(1L),
                () -> assertThat(response.content().get(0).content()).isEqualTo("테스트 코멘트")
            );
        }

        @Test
        void getComments_페이지네이션_적용() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();
            Comment comment = createMockComment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));
            PageRequest pageRequest = PageRequest.of(1, 10);
            Page<Comment> commentPage = new PageImpl<>(List.of(comment), pageRequest, 15);
            given(commentRepository.findByMomentId(any(Long.class), any(Pageable.class))).willReturn(commentPage);

            // when
            AdminCommentListResponse response = adminContentService.getComments(1L, 1L, 1, 10);

            // then
            assertAll(
                () -> assertThat(response.page()).isEqualTo(commentPage.getNumber()),
                () -> assertThat(response.size()).isEqualTo(commentPage.getSize()),
                () -> assertThat(response.totalElements()).isEqualTo(commentPage.getTotalElements()),
                () -> assertThat(response.totalPages()).isEqualTo(commentPage.getTotalPages())
            );
        }

        @Test
        void getComments_작성자_정보_포함() {
            // given
            Group group = mock(Group.class);
            Moment moment = createMockMoment();
            Comment comment = createMockComment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(moment));
            Page<Comment> commentPage = new PageImpl<>(List.of(comment), PageRequest.of(0, 20), 1);
            given(commentRepository.findByMomentId(any(Long.class), any(Pageable.class))).willReturn(commentPage);

            // when
            AdminCommentListResponse response = adminContentService.getComments(1L, 1L, 0, 20);

            // then
            assertAll(
                () -> assertThat(response.content().get(0).author().memberId()).isEqualTo(10L),
                () -> assertThat(response.content().get(0).author().groupNickname()).isEqualTo("그룹닉네임"),
                () -> assertThat(response.content().get(0).author().userId()).isEqualTo(100L),
                () -> assertThat(response.content().get(0).author().userEmail()).isEqualTo("user@test.com")
            );
        }

        @Test
        void getComments_모멘트없으면_예외() {
            // given
            Group group = mock(Group.class);

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(momentRepository.findByIdAndGroupId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.getComments(1L, 999L, 0, 20))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_MOMENT_NOT_FOUND);
        }

        @Test
        void getComments_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.getComments(999L, 1L, 0, 20))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 코멘트_삭제 {

        @Test
        void deleteComment_코멘트_SoftDelete_성공() {
            // given
            Group group = mock(Group.class);
            Comment comment = createMockComment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(commentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            adminContentService.deleteComment(1L, 1L, 1L, "admin@test.com");

            // then
            verify(commentRepository).delete(comment);
        }

        @Test
        void deleteComment_AdminGroupLog_기록_확인() {
            // given
            Group group = mock(Group.class);
            Comment comment = createMockComment();

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(commentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            adminContentService.deleteComment(1L, 1L, 1L, "admin@test.com");

            // then
            verify(adminGroupLogRepository).save(any());
        }

        @Test
        void deleteComment_이미_삭제된_코멘트_예외() {
            // given
            Group group = mock(Group.class);
            Comment comment = createMockComment();
            given(comment.getDeletedAt()).willReturn(LocalDateTime.now());

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(commentRepository.findByIdAndGroupId(1L, 1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteComment(1L, 1L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_COMMENT_ALREADY_DELETED);
        }

        @Test
        void deleteComment_코멘트없으면_예외() {
            // given
            Group group = mock(Group.class);

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(commentRepository.findByIdAndGroupId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteComment(1L, 999L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_COMMENT_NOT_FOUND);
        }

        @Test
        void deleteComment_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminContentService.deleteComment(999L, 1L, 1L, "admin@test.com"))
                .isInstanceOf(MomentException.class)
                .extracting(e -> ((MomentException) e).getErrorCode())
                .isEqualTo(ErrorCode.GROUP_NOT_FOUND);
        }
    }
}
