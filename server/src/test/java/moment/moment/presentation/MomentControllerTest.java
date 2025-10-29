package moment.moment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import moment.auth.application.TokenManager;
import moment.comment.dto.request.CommentCreateRequest;
import moment.common.DatabaseCleaner;
import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreationStatus;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MomentNotificationResponse;
import moment.moment.dto.response.MomentReportCreateResponse;
import moment.moment.dto.response.TagNamesResponse;
import moment.moment.dto.response.tobe.MyMomentPageResponse;
import moment.moment.dto.response.tobe.MyMomentResponse;
import moment.moment.infrastructure.MomentImageRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.moment.infrastructure.MomentTagRepository;
import moment.moment.infrastructure.TagRepository;
import moment.report.domain.Report;
import moment.report.domain.ReportReason;
import moment.report.infrastructure.ReportRepository;
import moment.support.MomentCreatedAtHelper;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
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
    private MomentImageRepository momentImageRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private MomentTagRepository momentTagRepository;
    @Autowired
    private MomentCreatedAtHelper momentCreatedAtHelper;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 기본_모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";
        List<String> tagNames = List.of("일상/여가");

        MomentCreateRequest request = new MomentCreateRequest(content, tagNames, null, null);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
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
        List<String> tagNames = List.of("일상/여가");

        MomentCreateRequest request = new MomentCreateRequest(content, tagNames, null, null);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
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
    void 이미지를_첨부한_기본_모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";
        List<String> tagNames = List.of("일상/여가");
        String imageUrl = "http://s3-north-east/techcourse-2025/moment-dev/images/abcde.jpg";
        String imageName = "abcde.jpg";

        MomentCreateRequest request = new MomentCreateRequest(content, tagNames, imageUrl, imageName);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
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
    void 이미지를_첨부한_추가_모멘트를_등록한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(10);
        User savedMomenter = userRepository.save(momenter);
        String content = "재미있는 내용이네요~~?";
        List<String> tagNames = List.of("일상/여가");
        String imageUrl = "http://s3-north-east/techcourse-2025/moment-dev/images/abcde.jpg";
        String imageName = "abcde.jpg";

        MomentCreateRequest request = new MomentCreateRequest(content, tagNames, imageUrl, imageName);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
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
    void 추가_모멘트를_등록_시_별조각을_차감한다() {

        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(30);
        User savedMomenter = userRepository.saveAndFlush(momenter);
        String content = "재미있는 내용이네요~~?";
        List<String> tagNames = List.of("일상/여가");

        MomentCreateRequest request = new MomentCreateRequest(content, tagNames, null, null);
        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        MomentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v1/moments")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreateResponse.class);

        User user = userRepository.findById(savedMomenter.getId()).get();
        // then
        assertAll(
                () -> assertThat(response.momenterId()).isEqualTo(savedMomenter.getId()),
                () -> assertThat(response.content()).isEqualTo(content),
                () -> assertThat(user.getAvailableStar()).isEqualTo(35)
        );

        String contentExtra = "추가 모멘트 재미있는 내용이네요~~?";

        MomentCreateRequest requestExtra = new MomentCreateRequest(contentExtra, tagNames, null, null);

        // when
        MomentCreateResponse responseExtra = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(requestExtra)
                .when().post("/api/v1/moments/extra")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreateResponse.class);

        User findUser = userRepository.findById(savedMomenter.getId()).get();
        // then
        assertAll(
                () -> assertThat(responseExtra.momenterId()).isEqualTo(savedMomenter.getId()),
                () -> assertThat(responseExtra.content()).isEqualTo(contentExtra),
                () -> assertThat(findUser.getAvailableStar()).isEqualTo(25)
        );
    }

    @Test
    @Disabled
    void 내_모멘트를_등록_시간_순으로_정렬한_페이지를_조회한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

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
        MyMomentPageResponse response = RestAssured.given().log().all()
                .param("limit", 3)
                .cookie("accessToken", token)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        String expectedNextCursor = cursorMoment.getCreatedAt().toString() + "_" + cursorMoment.getId();

        assertAll(
                () -> assertThat(response.items().myMomentsResponse()).hasSize(3),
                () -> assertThat(response.items().myMomentsResponse().stream()
                        .allMatch(item -> item.momenterId().equals(savedMomenter.getId())))
                        .isTrue(),
                () -> assertThat(response.items().myMomentsResponse())
                        .isSortedAccordingTo(Comparator.comparing(MyMomentResponse::createdAt).reversed()),
                () -> assertThat(response.nextCursor()).isEqualTo(expectedNextCursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(3)
        );
    }

    @Test
    void 내_모멘트_조회_시_읽음_상태를_함께_반환한다() {
        // given
        User momenter = new User("momenter@gmail.com", "1234", "momenter", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(10);
        User savedMomenter = userRepository.save(momenter);

        User commenter = userRepository.save(new User("commenter@gmail.com", "1234", "commenter", ProviderType.EMAIL));

        String momenterToken = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());
        String commenterToken = tokenManager.createAccessToken(commenter.getId(), commenter.getEmail());

        MomentCreateRequest createRequest1 = new MomentCreateRequest("첫 번째 모멘트", List.of("일상/여가"), null, null);
        MomentCreateResponse momentResponse1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("accessToken", momenterToken)
                .body(createRequest1)
                .when().post("/api/v1/moments")
                .then().extract().jsonPath().getObject("data", MomentCreateResponse.class);

        MomentCreateRequest createRequest2 = new MomentCreateRequest("두 번째 모멘트", List.of("일상/여가"), null, null);
        MomentCreateResponse momentResponse2 = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("accessToken", momenterToken)
                .body(createRequest2)
                .when().post("/api/v1/moments/extra")
                .then().extract().jsonPath().getObject("data", MomentCreateResponse.class);

        CommentCreateRequest commentRequest = new CommentCreateRequest("코멘트 내용", momentResponse2.id(), null, null);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("accessToken", commenterToken)
                .body(commentRequest)
                .when().post("/api/v1/comments")
                .then().statusCode(HttpStatus.CREATED.value());

        // when
        MyMomentPageResponse response = RestAssured.given().log().all()
                .param("limit", 5)
                .cookie("accessToken", momenterToken)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        List<MyMomentResponse> myMoments = response.items().myMomentsResponse();

        MomentNotificationResponse recentMomentNotificationResponse = myMoments.get(0).momentNotification();
        MomentNotificationResponse olderMomentNotificationResponse = myMoments.get(1).momentNotification();

        assertAll(
                () -> assertThat(recentMomentNotificationResponse.isRead()).isFalse(),
                () -> assertThat(recentMomentNotificationResponse.notificationIds()).isNotEmpty(),
                () -> assertThat(olderMomentNotificationResponse.isRead()).isTrue(),
                () -> assertThat(olderMomentNotificationResponse.notificationIds()).isEmpty()
        );
    }

    @Test
    void 내_모멘트_조회_시_모멘트_태그가_없는_경우도_조회된다() {
        // given
        User momenter = new User("momenter@gmail.com", "1234", "momenter", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(10);
        User savedMomenter = userRepository.save(momenter);

        String momenterToken = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        Moment momentWithoutTag = new Moment("태그 없는 모멘트", savedMomenter, WriteType.BASIC);
        momentRepository.save(momentWithoutTag);

        // when
        MyMomentPageResponse response = RestAssured.given().log().all()
                .param("limit", 5)
                .cookie("accessToken", momenterToken)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        List<MyMomentResponse> myMoments = response.items().myMomentsResponse();

        TagNamesResponse tagNames = myMoments.get(0).tagNames();

        assertThat(tagNames.getTagNames()).isEmpty();
    }

    @Test
    @Disabled
    void DB에_저장된_Moment가_limit보다_적을_경우_남은_목록을_반환한다() throws InterruptedException {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

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
        MyMomentPageResponse response = RestAssured.given().log().all()
                .param("limit", 10)
                .cookie("accessToken", token)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        assertAll(
                () -> assertThat(response.items().myMomentsResponse()).hasSize(4),
                () -> assertThat(response.items().myMomentsResponse().stream()
                        .allMatch(item -> item.momenterId().equals(savedMomenter.getId())))
                        .isTrue(),
                () -> assertThat(response.items().myMomentsResponse())
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

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
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

        Moment moment = new Moment("아 행복해", savedMomenter, WriteType.BASIC);
        momentRepository.save(moment);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v1/moments/writable/basic")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.DENIED);
    }

    @Test
    void 코멘트를_작성할_수_있는_모멘트를_조회한다() {
        // given
        User user = new User("mimi@gmail.com", "mimi1234!", "mimi", ProviderType.EMAIL);
        User savedUser = userRepository.save(user);

        User momenter = new User("hippo@gmail.com", "hippo1234!", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        // when
        CommentableMomentResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v1/moments/commentable")
                .jsonPath()
                .getObject("data", CommentableMomentResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(savedMoment.getId()),
                () -> assertThat(response.nickname()).isEqualTo(savedMomenter.getNickname()),
                () -> assertThat(response.level()).isEqualTo(savedMomenter.getLevel()),
                () -> assertThat(response.content()).isEqualTo(savedMoment.getContent())
        );
    }

    @Test
    void 코멘트를_작성할_수_있는_이미지를_포함한_모멘트를_조회한다() {
        // given
        User user = new User("mimi@gmail.com", "mimi1234!", "mimi", ProviderType.EMAIL);
        User savedUser = userRepository.save(user);

        User momenter = new User("hippo@gmail.com", "hippo1234!", "hippo", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        String imageUrl = "http://s3-north-east/techcourse-2025/moment-dev/images/abcde.jpg";
        String imageName = "abcde.jpg";
        MomentImage momentImage = new MomentImage(savedMoment, imageUrl, imageName);
        momentImageRepository.save(momentImage);

        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        // when
        CommentableMomentResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v1/moments/commentable")
                .jsonPath()
                .getObject("data", CommentableMomentResponse.class);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(savedMoment.getId()),
                () -> assertThat(response.nickname()).isEqualTo(savedMomenter.getNickname()),
                () -> assertThat(response.level()).isEqualTo(savedMomenter.getLevel()),
                () -> assertThat(response.content()).isEqualTo(savedMoment.getContent()),
                () -> assertThat(response.imageUrl()).isEqualTo(imageUrl)
        );
    }

    @Test
    void 추가_모멘트_작성_가능_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(10);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v1/moments/writable/extra")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.ALLOWED);
    }

    @Test
    void 추가_모멘트_작성_불가능_상태를_가져온다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(9);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        // when
        MomentCreationStatusResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("api/v1/moments/writable/extra")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MomentCreationStatusResponse.class);

        // then
        assertThat(response.status()).isEqualTo(MomentCreationStatus.DENIED);
    }

    @Test
    void 모멘트를_신고한다() {
        // given
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(9);
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        MomentReportCreateRequest request = new MomentReportCreateRequest("SEXUAL_CONTENT");

        // when
        MomentReportCreateResponse momentReportCreateResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("api/v1/moments/1/reports")
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
        User momenter = new User("hippo@gmail.com", "1234", "hippo", ProviderType.EMAIL);
        momenter.addStarAndUpdateLevel(9);
        User savedMomenter = userRepository.save(momenter);

        Moment moment = new Moment("아 행복해", savedMomenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(moment);

        User reporter1 = new User("ddd@gmail.com", "1234!", "드라고", ProviderType.EMAIL);
        User reporter2 = new User("hhh@gmail.com", "1234!", "히포", ProviderType.EMAIL);
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
                .when().post("api/v1/moments/" + savedMoment.getId() + "/reports")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        // then
        Optional<Moment> result = momentRepository.findById(savedMoment.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void 나의_Moment_목록을_조회한다() {
        // given
        User momenter = new User("kiki@icloud.com", "1234", "kiki", ProviderType.EMAIL);
        User savedMomenter = userRepository.save(momenter);

        String token = tokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Moment savedMoment = momentCreatedAtHelper.saveMomentWithCreatedAt("오늘 하루는 힘든 하루~", savedMomenter,
                WriteType.BASIC, start);
        Tag savedTag = tagRepository.save(new Tag("일상/생각"));
        momentTagRepository.save(new MomentTag(savedMoment, savedTag));

        Moment savedMoment2 = momentCreatedAtHelper.saveMomentWithCreatedAt("오늘 하루는 즐거운 하루~", savedMomenter,
                WriteType.BASIC, start.plusHours(1));
        momentTagRepository.save(new MomentTag(savedMoment2, savedTag));

        // when
        MyMomentPageResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .param("limit", 1)
                .when().get("/api/v1/moments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyMomentPageResponse.class);

        // then
        List<MyMomentResponse> myMoments = response.items().myMomentsResponse();
        MyMomentResponse firstResponse = myMoments.getFirst();

        String cursor = savedMoment2.getCreatedAt().toString() + "_" + savedMoment2.getId();

        assertAll(
                () -> assertThat(myMoments).hasSize(1),
                () -> assertThat(response.nextCursor()).isEqualTo(cursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(1),
                () -> assertThat(firstResponse.content()).isEqualTo(savedMoment2.getContent()),
                () -> assertThat(firstResponse.content()).isEqualTo(savedMoment2.getContent())
        );
    }
}
