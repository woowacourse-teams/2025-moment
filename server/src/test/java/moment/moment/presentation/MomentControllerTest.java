package moment.moment.presentation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.matching.domain.Matching;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        String content = "재미있는 내용이네요~~?";

        MomentCreateRequest request = new MomentCreateRequest(content);
        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/api/v1/moments")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreateResponse.class);

        // then
        assertAll(
                () -> assertThat(response.momenterId()).isEqualTo(savedMomenter.getId()),
                () -> assertThat(response.content()).isEqualTo(content)
        );
    }

    @Test
    @Disabled
    void 내_모멘트를_등록_시간_순으로_정렬한_페이지를_조회한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        Moment moment1 = new Moment("아 행복해", true, savedMomenter);
        Moment moment2 = new Moment("아 힘들어", true, savedMomenter);
        Moment moment3 = new Moment("아 짜증나", false, savedMomenter);
        Moment moment4 = new Moment("아 신기해", false, savedMomenter);

        momentRepository.save(moment1);
        Thread.sleep(200);
        momentRepository.save(moment2);
        Thread.sleep(200);
        momentRepository.save(moment3);
        Thread.sleep(200);
        Moment saveMoment4 = momentRepository.save(moment4);

        // when
        MyMomentPageResponse response = RestAssured.given().log().all()
                .param("limit", 2)
                .cookie("token", token)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        String expectedNextCursor = saveMoment4.getCreatedAt().toString() + "_" + saveMoment4.getId();

        assertAll(
                () -> assertThat(response.items()).hasSize(2),
                () -> assertThat(response.items().stream()
                        .allMatch(item -> item.momenterId().equals(savedMomenter.getId())))
                        .isTrue(),
                () -> assertThat(response.items())
                        .isSortedAccordingTo(Comparator.comparing(MyMomentResponse::createdAt).reversed()),
                () -> assertThat(response.nextCursor()).isEqualTo(expectedNextCursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(2)
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
        matchingRepository.save(matching);

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

    @Test
    void 모멘트_생성가능_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("api/v1/moments/me/creation-status")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 모멘트_생성불가_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo");
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", true, savedMomenter);
        momentRepository.save(moment);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("api/v1/moments/me/creation-status")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.DENIED);
    }
}
