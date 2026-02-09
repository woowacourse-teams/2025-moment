package moment.moment.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import moment.comment.service.application.CommentApplicationService;
import moment.group.service.group.GroupMemberService;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.service.application.MomentApplicationService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentableMomentFacadeServiceTest {

    @InjectMocks
    private CommentableMomentFacadeService commentableMomentFacadeService;

    @Mock
    private MomentApplicationService momentApplicationService;

    @Mock
    private CommentApplicationService commentApplicationService;

    @Mock
    private GroupMemberService groupMemberService;

    @Test
    void 댓글_달_수_있는_모멘트가_없으면_empty를_반환한다() {
        // given
        Long groupId = 1L;
        Long commenterId = 2L;

        given(momentApplicationService.getCommentableMomentIdsInGroup(groupId, commenterId))
                .willReturn(Collections.emptyList());

        // when
        CommentableMomentResponse result = commentableMomentFacadeService.getCommentableMomentInGroup(
                groupId, commenterId);

        // then
        assertThat(result).isNull();
        verify(commentApplicationService, never()).getMomentIdsNotCommentedByMe(
                Collections.emptyList(), commenterId);
    }

    @Test
    void 모든_모멘트에_이미_댓글을_달았으면_빈_응답을_반환한다() {
        // given
        Long groupId = 1L;
        Long commenterId = 2L;
        List<Long> momentIds = List.of(10L, 20L, 30L);

        given(momentApplicationService.getCommentableMomentIdsInGroup(groupId, commenterId))
                .willReturn(momentIds);
        given(commentApplicationService.getMomentIdsNotCommentedByMe(momentIds, commenterId))
                .willReturn(Collections.emptyList());
        given(momentApplicationService.pickRandomMomentComposition(Collections.emptyList()))
                .willReturn(CommentableMomentResponse.empty());

        // when
        CommentableMomentResponse result = commentableMomentFacadeService.getCommentableMomentInGroup(
                groupId, commenterId);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 댓글_달_수_있는_모멘트가_있으면_랜덤으로_하나를_반환한다() {
        // given
        Long groupId = 1L;
        Long commenterId = 2L;
        List<Long> momentIds = List.of(10L, 20L, 30L);
        List<Long> notCommentedIds = List.of(20L, 30L);
        CommentableMomentResponse expected = new CommentableMomentResponse(
                20L, null, "작성자", "모멘트 내용", null, null);

        given(momentApplicationService.getCommentableMomentIdsInGroup(groupId, commenterId))
                .willReturn(momentIds);
        given(commentApplicationService.getMomentIdsNotCommentedByMe(momentIds, commenterId))
                .willReturn(notCommentedIds);
        given(momentApplicationService.pickRandomMomentComposition(notCommentedIds))
                .willReturn(expected);

        // when
        CommentableMomentResponse result = commentableMomentFacadeService.getCommentableMomentInGroup(
                groupId, commenterId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(20L);
        assertThat(result.content()).isEqualTo("모멘트 내용");
    }
}
