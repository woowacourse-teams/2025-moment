package moment.comment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.infrastructure.JwtTokenManager;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EmojiRepository emojiRepository;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Test
    void Comment_생성에_성공한다() {
        // given
        String token = jwtTokenManager.createToken(1L, "hippo@gmail.com");

        User user1 = new User("hippo@gmail.com", "1234", "hippo");
        userRepository.saveAndFlush(user1);

        User user2 = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.saveAndFlush(user2);

        Moment moment = new Moment("개발의 세계는 신비해요!", true, user2);
        momentRepository.saveAndFlush(moment);

        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L);

        CommentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/api/v1/comments")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", CommentCreateResponse.class);

        // then
        assertThatCode(
                () -> commentRepository.findById(response.commentId())
                        .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND)))
                .doesNotThrowAnyException();
    }

    @Test
    void 나의_Comment_목록을_조회한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki");
        User savedMomenter = userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedCommenter = userRepository.save(commenter);

        String token = jwtTokenManager.createToken(savedCommenter.getId(), savedCommenter.getEmail());

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        Comment comment = new Comment("첫 번째 댓글", savedCommenter, savedMoment);
        Comment savedComment = commentRepository.save(comment);

        Emoji emoji = new Emoji(EmojiType.HEART, savedMomenter, savedComment);
        Emoji savedEmoji = emojiRepository.save(emoji);

        // when
        List<MyCommentsResponse> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/api/v1/comments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("data", MyCommentsResponse.class);

        // then
        MyCommentsResponse firstResponse = response.getFirst();

        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment.getContent()),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment.getContent()),
                () -> assertThat(firstResponse.moment().content()).isEqualTo(savedMoment.getContent()),
                () -> assertThat(firstResponse.emojis().getFirst().id()).isEqualTo(savedEmoji.getId()),
                () -> assertThat(firstResponse.emojis().getFirst().emojiType()).isEqualTo(savedEmoji.getEmojiType())
        );
    }
}
