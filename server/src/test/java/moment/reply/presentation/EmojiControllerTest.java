package moment.reply.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.application.TokenManager;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.reply.dto.response.EmojiReadResponse;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class EmojiControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private MomentRepository momentRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EmojiRepository emojiRepository;

    private User momenter;
    private User commenter;
    private String commenterToken;
    private Moment moment;
    private Comment comment;

    @BeforeEach
    void setUp() {
        momenter = userRepository.save(new User("kiki@gmail.com", "1234", "kiki"));
        commenter = userRepository.save(new User("drago@gmail.com", "1234", "drago"));
        commenterToken = tokenManager.createToken(commenter.getId(), commenter.getEmail());
        moment = momentRepository.save(new Moment("아 행복해", true, momenter));
        comment = commentRepository.save(new Comment("행복하지마요~", commenter, moment));
    }

    @Test
    void 이모지를_등록한다() {
        // given
        String emojiType = "HEART";
        EmojiCreateRequest request = new EmojiCreateRequest(emojiType, comment.getId());

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", commenterToken)
            .body(request)
            .when().post("/api/v1/emojis")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value());

        // then
        List<Emoji> emojis = emojiRepository.findAllByComment(comment);
        assertAll(
            () -> assertThat(emojis).hasSize(1),
            () -> assertThat(emojis.get(0).getEmojiType()).isEqualTo(emojiType),
            () -> assertThat(emojis.get(0).getUser().getId()).isEqualTo(commenter.getId()),
            () -> assertThat(emojis.get(0).getComment().getId()).isEqualTo(comment.getId())
        );
    }

    @Test
    void 이모지를_조회한다() {
        // given
        Emoji savedEmoji = emojiRepository.save(new Emoji("HEART", momenter, comment));

        // when
        List<EmojiReadResponse> response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", commenterToken)
            .when().get("/api/v1/emojis/" + comment.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().jsonPath()
            .getList("data", EmojiReadResponse.class);

        // then
        assertAll(
            () -> assertThat(response).hasSize(1),
            () -> assertThat(response.get(0).emojiType()).isEqualTo(savedEmoji.getEmojiType()),
            () -> assertThat(response.get(0).userName()).isEqualTo(savedEmoji.getUser().getNickname())
        );
    }

    @Test
    void 이모지를_삭제한다() {
        // given
        Emoji savedEmoji = emojiRepository.save(new Emoji("HEART", commenter, comment));
        String emojiOwnerToken = tokenManager.createToken(commenter.getId(), commenter.getEmail());

        // when & then
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", emojiOwnerToken)
            .delete("/api/v1/emojis/" + savedEmoji.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
