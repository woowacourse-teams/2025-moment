package moment.comment.dto.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import moment.comment.domain.Comment;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentCompositionTest {

    @Test
    void commenter가_null인_경우_탈퇴한_사용자로_표시된다() {
        // given
        User commenter = null;
        Comment comment = createCommentStub();

        // when
        CommentComposition composition = CommentComposition.of(comment, commenter, "imageUrl");

        // then
        assertThat(composition.nickname()).isEqualTo("탈퇴한 사용자");
    }

    @Test
    void commenter가_존재하는_경우_닉네임이_정상_표시된다() {
        // given
        User commenter = UserFixture.createUserWithId(1L);
        Comment comment = createCommentStub();

        // when
        CommentComposition composition = CommentComposition.of(comment, commenter, "imageUrl");

        // then
        assertThat(composition.nickname()).isEqualTo(commenter.getNickname());
    }

    @Test
    void member가_null인_경우_memberId가_null이다() {
        // given
        Comment comment = createCommentStub();
        User commenter = UserFixture.createUserWithId(1L);

        // when
        CommentComposition composition = CommentComposition.of(comment, commenter, "imageUrl");

        // then
        assertThat(composition.memberId()).isNull();
    }

    private Comment createCommentStub() {
        User dummyUser = UserFixture.createUserWithId(99L);
        return new Comment("테스트 댓글", dummyUser, 1L);
    }
}
