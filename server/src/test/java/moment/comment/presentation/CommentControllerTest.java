package moment.comment.presentation;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.infrastructure.JwtTokenManager;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
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
@Disabled
class CommentControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Test
    void Comment_생성에_성공한다() {
        // given
        String token = jwtTokenManager.createToken(1L, "hippo@gmail.com");

        User user1 = new User("hippo@gmail.com", "1234", "hippo");
        userRepository.save(user1);

        User user2 = new User("kiki@icloud.com", "1234", "kiki");
        userRepository.save(user2);

        Moment moment = new Moment("개발의 세계는 신비해요!", true, user2);
        momentRepository.save(moment);

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
}
