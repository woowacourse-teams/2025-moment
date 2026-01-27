package moment.moment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Optional;
import moment.auth.application.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MomentReportCreateResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.report.infrastructure.ReportRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@org.junit.jupiter.api.Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MomentRepository momentRepository;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 기본_모멘트를_등록한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.saveAndFlush(momenter);
        String content = "재미있는 내용이네요~~?";

        MomentCreateRequest request = new MomentCreateRequest(content, null, null);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v2/moments")
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
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";

        MomentCreateRequest request = new MomentCreateRequest(content, null, null);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v2/moments/extra")
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
    void 이미지를_첨부한_기본_모멘트를_등록한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";
        String imageUrl = "http://s3-north-east/techcourse-2025/moment-dev/images/abcde.jpg";
        String imageName = "abcde.jpg";

        MomentCreateRequest request = new MomentCreateRequest(content, imageUrl, imageName);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v2/moments")
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
    void 이미지를_첨부한_추가_모멘트를_등록한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";
        String imageUrl = "http://s3-north-east/techcourse-2025/moment-dev/images/abcde.jpg";
        String imageName = "abcde.jpg";

        MomentCreateRequest request = new MomentCreateRequest(content, imageUrl, imageName);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v2/moments/extra")
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
    void 기본_모멘트_작성_가능_상태를_가져온다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v2/moments/writable/basic")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 기본_모멘트_작성_항상_가능_상태를_반환한다() {
        // given (정책 제거로 기본 모멘트 작성은 항상 허용)
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter);
        momentRepository.save(moment);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v2/moments/writable/basic")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 추가_모멘트_작성_가능_상태를_가져온다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v2/moments/writable/extra")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 추가_모멘트_작성_가능_상태를_항상_반환한다() {
        // given (stars 시스템 제거로 extra moment는 항상 허용)
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v2/moments/writable/extra")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 모멘트를_신고한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        MomentReportCreateRequest request = new MomentReportCreateRequest("SEXUAL_CONTENT");

        // when
        MomentReportCreateResponse momentReportCreateResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("api/v2/moments/1/reports")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentReportCreateResponse.class);

        // then
        assertThat(momentReportCreateResponse.id()).isEqualTo(1L);
    }

    @Test
    void 정해진_신고_횟수_넘긴_모멘트를_삭제한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        User reporter1 = UserFixture.createUser();
        User reporter2 = UserFixture.createUser();
        User savedReporter1 = userRepository.save(reporter1);
        User savedReporter2 = userRepository.save(reporter2);

        TargetType targetType = TargetType.MOMENT;
        Report report1 = new Report(savedReporter1, targetType, savedMoment.getId(), ReportReason.ABUSE_OR_HARASSMENT);
        Report report2 = new Report(savedReporter2, targetType, savedMoment.getId(), ReportReason.ABUSE_OR_HARASSMENT);

        reportRepository.save(report1);
        reportRepository.save(report2);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        MomentReportCreateRequest request = new MomentReportCreateRequest("SEXUAL_CONTENT");

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("api/v2/moments/" + savedMoment.getId() + "/reports")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        // then
        Optional<Moment> result = momentRepository.findById(savedMoment.getId());
        assertThat(result).isEmpty();
    }
}
