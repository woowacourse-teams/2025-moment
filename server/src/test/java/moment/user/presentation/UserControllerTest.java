package moment.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import moment.auth.application.TokenManager;
import moment.auth.dto.google.GoogleUserInfo;
import moment.common.DatabaseCleaner;
import moment.global.dto.response.SuccessResponse;
import moment.user.application.PendingUserCacheService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.BasicUserCreateRequest;
import moment.user.dto.request.GoogleOAuthUserCreateRequest;
import moment.user.dto.request.NicknameConflictCheckRequest;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.dto.response.NicknameConflictCheckResponse;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private PendingUserCacheService pendingUserCacheService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 일반_회원가입시_유저_생성에_성공한다() {
        // given
        BasicUserCreateRequest request = new BasicUserCreateRequest("mimi@icloud.com", "mimi1234!", "mimi1234!",
                "mimi", false);

        // when
        SuccessResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/users/signup")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(SuccessResponse.class);

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(
                        userRepository.existsByEmailAndProviderType("mimi@icloud.com", ProviderType.EMAIL)).isTrue()
        );
    }

    @Test
    void 구글_회원가입시_유저_생성_및_로그인에_성공하고_토큰을_반환한다() {
        // given
        GoogleOAuthUserCreateRequest request = new GoogleOAuthUserCreateRequest("mimi@icloud.com", "mimi1234!",
                "mimi1234!",
                "mimi", false);

        pendingUserCacheService.register(
                new GoogleUserInfo("sub", "name", "giveName", "picture", "mimi@icloud.com", false));

        String pendingToken = tokenManager.createPendingToken("mimi@icloud.com");

        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("pendingToken", pendingToken)
                .body(request)
                .when().post("/api/v1/users/signup/google")
                .then().log().all()
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value())
                .extract().response();

        // then
        String accessToken = response.getCookie("accessToken");
        String refreshToken = response.getCookie("refreshToken");

        assertAll(
                () -> assertThat(
                        userRepository.existsByEmailAndProviderType("mimi@icloud.com", ProviderType.GOOGLE)).isTrue(),
                () -> assertThat(accessToken).isNotBlank(),
                () -> assertThat(refreshToken).isNotBlank()
        );
    }

    @Test
    void 유저_프로필_조회에_성공한다() {
        // given
        String nickname = "mimi";
        User user = userRepository.save(new User("mimi@icloud.com", "password", nickname, ProviderType.EMAIL));
        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());
        UserProfileResponse expect = new UserProfileResponse(nickname, user.getAvailableStar(), user.getLevel(),
                user.getLevel().getNextLevelRequiredStars());

        // when
        SuccessResponse<UserProfileResponse> response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("/api/v1/users/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.data()).isEqualTo(expect)
        );
    }

    @Test
    void 닉네임_중복_여부_조회를_성공한다() {
        // given
        String nickname = "mimi";
        NicknameConflictCheckRequest request = new NicknameConflictCheckRequest(nickname);

        userRepository.save(new User("mimi@icloud.com", "password", nickname, ProviderType.EMAIL));

        // when
        SuccessResponse<NicknameConflictCheckResponse> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/users/signup/nickname/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        NicknameConflictCheckResponse expect = new NicknameConflictCheckResponse(true);

        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.data()).isEqualTo(expect)
        );
    }


    @Test
    void 회원가입에_사용_가능한_랜덤_닉네임_조회에_성공한다() {
        // given
        SuccessResponse<MomentRandomNicknameResponse> response = RestAssured.given().log().all()
                .when().get("/api/v1/users/signup/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.data().randomNickname()).isNotBlank()
        );
    }
}
