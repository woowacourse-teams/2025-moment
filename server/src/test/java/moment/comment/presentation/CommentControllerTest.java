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
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.dto.response.MyCommentResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Echo;
import moment.reply.infrastructure.EchoRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Disabled;
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
    private EchoRepository echoRepository;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Test
    void Comment_생성에_성공한다() {
        // given
        String token = jwtTokenManager.createAccessToken(1L, "hippo@gmail.com");

        User user1 = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        userRepository.saveAndFlush(user1);

        User user2 = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        userRepository.saveAndFlush(user2);

        Moment moment = new Moment("개발의 세계는 신비해요!", true, user2, WriteType.BASIC);
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
    @Disabled
    void 나의_Comment_목록을_조회한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        User commenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedCommenter = userRepository.save(commenter);

        String token = jwtTokenManager.createAccessToken(savedCommenter.getId(), savedCommenter.getEmail());

        Moment moment = new Moment("오늘 하루는 힘든 하루~", true, savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        Comment comment = new Comment("첫 번째 댓글", savedCommenter, savedMoment);
        Comment savedComment = commentRepository.save(comment);

        Echo echo = new Echo("HEART", savedMomenter, savedComment);
        Echo savedEcho = echoRepository.save(echo);

        Moment moment2 = new Moment("오늘 하루는 즐거운 하루~", true, savedMomenter, WriteType.BASIC);
        Moment savedMoment2 = momentRepository.save(moment2);

        Comment comment2 = new Comment("즐거운 댓글", savedCommenter, savedMoment2);
        Comment savedComment2 = commentRepository.save(comment2);

        Echo echo2 = new Echo("HEART", savedMomenter, savedComment2);
        Echo savedEcho2 = echoRepository.save(echo2);

        // when
        MyCommentPageResponse response = RestAssured.given().log().all()
                .cookie("token", token)
                .param("limit", 1)
                .when().get("/api/v1/comments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyCommentPageResponse.class);

        // then
        List<MyCommentResponse> myComments = response.items();
        MyCommentResponse firstResponse = myComments.getFirst();

        String cursor = savedComment2.getCreatedAt().toString() + "_" + savedComment2.getId();

        assertAll(
                () -> assertThat(myComments).hasSize(1),
                () -> assertThat(response.nextCursor()).isEqualTo(cursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(1),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment2.getContent()),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment2.getContent()),
                () -> assertThat(firstResponse.moment().content()).isEqualTo(savedMoment2.getContent()),
                () -> assertThat(firstResponse.echos().getFirst().id()).isEqualTo(savedEcho2.getId()),
                () -> assertThat(firstResponse.echos().getFirst().emojiType()).isEqualTo(savedEcho2.getEchoType())
        );
    }
}
