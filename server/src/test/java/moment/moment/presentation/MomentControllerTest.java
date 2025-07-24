package moment.moment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.global.dto.response.SuccessResponse;
import moment.matching.domain.Matching;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
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
class MomentControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private TokenManager tokenManager;

    @Test
    void 모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        MomentCreateRequest request = new MomentCreateRequest("재미있는 내용이네요~~?");
        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        SuccessResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/api/v1/moments")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(SuccessResponse.class);

        // then
        assertAll(() -> assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value())
        );
    }

    @Test
    void 매칭된_모멘트를_조회한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        User commenter = new User("kiki@gmail.com", "1234", "kiki");
        User savedCommenter = userRepository.save(commenter);

        String token = tokenManager.createToken(savedCommenter.getId(), savedCommenter.getEmail());

        Moment moment = new Moment("아 행복해", true, savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        Matching matching = new Matching(moment, commenter);
        Matching savedMatching = matchingRepository.save(matching);

        // when
        MatchedMomentResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/api/v1/moments/matching")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MatchedMomentResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(savedMoment.getId()),
                () -> assertThat(response.content()).isEqualTo(savedMoment.getContent())
        );


    }
}
