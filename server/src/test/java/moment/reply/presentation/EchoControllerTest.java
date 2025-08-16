package moment.reply.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Set;
import moment.auth.application.TokenManager;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Echo;
import moment.reply.dto.request.EchoCreateRequest;
import moment.reply.dto.response.EchoReadResponse;
import moment.reply.infrastructure.EchoRepository;
import moment.user.domain.ProviderType;
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
public class EchoControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EchoRepository echoRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private User momenter;
    private User commenter;
    private String momenterToken;
    private String commenterToken;
    private Moment moment;
    private Comment comment;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        momenter = userRepository.save(new User("kiki@gmail.com", "1234", "kiki", ProviderType.EMAIL));
        commenter = userRepository.save(new User("drago@gmail.com", "1234", "drago", ProviderType.EMAIL));
        momenterToken = tokenManager.createToken(momenter.getId(), momenter.getEmail());
        commenterToken = tokenManager.createToken(commenter.getId(), commenter.getEmail());
        moment = momentRepository.save(new Moment("아 행복해", true, momenter, WriteType.BASIC));
        comment = commentRepository.save(new Comment("행복하지마요~", commenter, moment));
    }

    @Test
    void 에코를_등록한다() {
        // given
        Set<String> echoTypes = Set.of("HEART");
        EchoCreateRequest request = new EchoCreateRequest(echoTypes, comment.getId());

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", momenterToken)
                .body(request)
                .when().post("/api/v1/echos")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        // then
        List<Echo> echoes = echoRepository.findAllByComment(comment);
        assertAll(
                () -> assertThat(echoes).hasSize(1),
                () -> assertThat(echoes.get(0).getEchoType()).isEqualTo("HEART"),
                () -> assertThat(echoes.get(0).getUser().getId()).isEqualTo(momenter.getId()),
                () -> assertThat(echoes.get(0).getComment().getId()).isEqualTo(momenter.getId())
        );
    }

    @Test
    void 에코를_조회한다() {
        // given
        Echo savedEcho = echoRepository.save(new Echo("HEART", momenter, comment));

        // when
        List<EchoReadResponse> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", commenterToken)
                .when().get("/api/v1/echos/" + comment.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath()
                .getList("data", EchoReadResponse.class);

        // then
        assertAll(
                () -> assertThat(response).hasSize(1),
                () -> assertThat(response.get(0).emojiType()).isEqualTo(savedEcho.getEchoType()),
                () -> assertThat(response.get(0).userName()).isEqualTo(savedEcho.getUser().getNickname())
        );
    }
}
