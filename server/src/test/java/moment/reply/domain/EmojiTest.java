package moment.reply.domain;


import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class EmojiTest {

    @Test
    void 코멘트가_잆는_경우_예외가_발생한다() {
        // given
        User user = new User("lebron@gmail.com", "1234", "르브론");

        // when & then
        assertThatThrownBy(() -> new Emoji(EmojiType.HEART, user, null))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_INVALID);
    }

    @Test
    void 이모지_작성자가_없는_경우_예외가_발생한다() {
        // given
        User momenter = new User("ekorea623@gmail.com", "1q2w3e4r", "drago");
        Moment moment = new Moment("오운완!", false, momenter);
        User commenter = new User("ama@gmail.com", "1234", "ama");
        Comment comment = new Comment("오운완!", commenter, moment);

        // when & then
        assertThatThrownBy(() -> new Emoji(EmojiType.HEART, null, comment))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}