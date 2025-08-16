package moment.moment.presentation;

import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
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

import static io.restassured.RestAssured.given;
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
    private TokenManager tokenManager;

    @Test
    void 기본_모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";

        MomentCreateRequest request = new MomentCreateRequest(content);
        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = given().log().all()
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
    void 추가_모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(10);
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";

        MomentCreateRequest request = new MomentCreateRequest(content);
        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/api/v1/moments/extra")
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
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        Moment moment1 = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        Moment moment2 = new Moment("아 힘들어", true, savedMomenter, WriteType.BASIC);
        Moment moment3 = new Moment("아 짜증나", false, savedMomenter, WriteType.BASIC);
        Moment moment4 = new Moment("아 신기해", false, savedMomenter, WriteType.BASIC);

        momentRepository.save(moment1);
        Thread.sleep(200);
        Moment cursorMoment = momentRepository.save(moment2);
        Thread.sleep(200);
        momentRepository.save(moment3);
        Thread.sleep(200);
        momentRepository.save(moment4);

        // when
        MyMomentPageResponse response = given().log().all()
                .param("limit", 3)
                .cookie("token", token)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        String expectedNextCursor = cursorMoment.getCreatedAt().toString() + "_" + cursorMoment.getId();

        assertAll(
                () -> assertThat(response.items()).hasSize(3),
                () -> assertThat(response.items().stream()
                        .allMatch(item -> item.momenterId().equals(savedMomenter.getId())))
                        .isTrue(),
                () -> assertThat(response.items())
                        .isSortedAccordingTo(Comparator.comparing(MyMomentResponse::createdAt).reversed()),
                () -> assertThat(response.nextCursor()).isEqualTo(expectedNextCursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(3)
        );
    }

    @Test
    @Disabled
    void DB에_저장된_Moment가_limit보다_적을_경우_남은_목록을_반환한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        Moment moment1 = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        Moment moment2 = new Moment("아 힘들어", true, savedMomenter, WriteType.BASIC);
        Moment moment3 = new Moment("아 짜증나", false, savedMomenter, WriteType.BASIC);
        Moment moment4 = new Moment("아 신기해", false, savedMomenter, WriteType.BASIC);

        momentRepository.save(moment1);
        Thread.sleep(200);
        Moment cursorMoment = momentRepository.save(moment2);
        Thread.sleep(200);
        momentRepository.save(moment3);
        Thread.sleep(200);
        momentRepository.save(moment4);

        // when
        MyMomentPageResponse response = given().log().all()
                .param("limit", 10)
                .cookie("token", token)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        assertAll(
                () -> assertThat(response.items()).hasSize(4),
                () -> assertThat(response.items().stream()
                        .allMatch(item -> item.momenterId().equals(savedMomenter.getId())))
                        .isTrue(),
                () -> assertThat(response.items())
                        .isSortedAccordingTo(Comparator.comparing(MyMomentResponse::createdAt).reversed()),
                () -> assertThat(response.nextCursor()).isNull(),
                () -> assertThat(response.hasNextPage()).isFalse(),
                () -> assertThat(response.pageSize()).isEqualTo(4)
        );
    }

    @Test
    void 기본_모멘트_작성_가능_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = given().log().all()
                .cookie("token", token)
                .when().get("api/v1/moments/writable/basic")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 기본_모멘트_작성_불가능_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", true, savedMomenter, WriteType.BASIC);
        momentRepository.save(moment);

        String token = tokenManager.createToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = given().log().all()
                .cookie("token", token)
                .when().get("api/v1/moments/writable/basic")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.DENIED);
    }

    // TODO: 추가 모멘트 작성 가능 상태 확인 api에 대한 테스트를 user의 포인트를 조정하는 로직이 들어오면 작성할 예정
}
