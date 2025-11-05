package moment.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import moment.auth.application.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.dto.response.SuccessResponse;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.Level;
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MyPageControllerTest {

    @Autowired
    PasswordEncoder passwordEncoder;
    @LocalServerPort
    private int port;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    private TokenManager tokenManager;

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
    void 유저_프로필_정보를_조회한다() {
        // given
        User user = UserFixture.createUserByEmailAndNickname("test@gmail.com", "신비로운 행성의 지구");
        userRepository.save(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // when
        SuccessResponse<MyPageProfileResponse> response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("/api/v1/me/profile")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        MyPageProfileResponse profile = response.data();
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(profile.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(profile.email()).isEqualTo(user.getEmail()),
                () -> assertThat(profile.level()).isEqualTo(Level.ASTEROID_WHITE),
                () -> assertThat(profile.availableStar()).isEqualTo(0),
                () -> assertThat(profile.expStar()).isEqualTo(0),
                () -> assertThat(profile.nextStepExp()).isEqualTo(5)
        );
    }

    @Test
    void 유저_보상_기록을_페이징_처리하여_조회한다() {
        // given
        User user = UserFixture.createUserByEmailAndNickname("test@gmail.com", "신비로운 행성의 지구");
        userRepository.save(user);

        createTestRewardHistory(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // when
        SuccessResponse<MyRewardHistoryPageResponse> response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .param("pageNum", 1)
                .param("pageSize", 10)
                .when().get("/api/v1/me/reward/history")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        MyRewardHistoryPageResponse pageData = response.data();

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(pageData.totalPages()).isEqualTo(2),
                () -> assertThat(pageData.pageSize()).isEqualTo(10),
                () -> assertThat(pageData.items().size()).isEqualTo(10),
                () -> assertThat(pageData.currentPageNum()).isEqualTo(1)
        );
    }

    private void createTestRewardHistory(User user) {
        for (int i = 0; i < 20; i++) {
            rewardRepository.save(
                    new RewardHistory(user, Reason.MOMENT_CREATION, (long) i));
        }
    }

    @Test
    void 마이페이지에서_유저_닉네임을_변경한다() {
        // given
        User user = UserFixture.createUserByEmailAndNickname("test@gmail.com", "신비로운 행성의 지구");
        ReflectionTestUtils.setField(user, "availableStar", 150);
        User savedUser = userRepository.save(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        NicknameChangeRequest request = new NicknameChangeRequest("변경될 유저의 닉네임");

        // when
        SuccessResponse<NicknameChangeResponse> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", token)
                .when().post("/api/v1/me/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        User nicknameChangedUser = userRepository.findById(savedUser.getId()).get();

        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(nicknameChangedUser.getNickname()).isEqualTo(request.newNickname()),
                () -> assertThat(nicknameChangedUser.getAvailableStar()).isEqualTo(50)
        );
    }

    @Test
    void 마이페이지_내에서_유저_비밀번호를_변경한다() {
        // given
        String encodePassword = passwordEncoder.encode("test123!@#");
        User user = userRepository.save(UserFixture.createUserByPassword(encodePassword));
        String email = user.getEmail();

        String accessToken = tokenManager.createAccessToken(user.getId(), email);
        String refreshToken = tokenManager.createRefreshToken(user.getId(), email);

        ChangePasswordRequest request = new ChangePasswordRequest("change123!@#", "change123!@#");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v1/me/password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        SuccessResponse<Void> body = response.as(new TypeRef<>() {
        });

        User changePasswordUser = userRepository.findById(user.getId()).get();

        // then
        assertAll(
                () -> assertThat(response.getCookie("accessToken")).isEmpty(),
                () -> assertThat(response.getCookie("refreshToken")).isEmpty(),
                () -> assertThat(body).isNotNull(),
                () -> assertThat(body.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(
                        passwordEncoder.matches(user.getPassword(), changePasswordUser.getPassword())).isFalse(),
                () -> assertThat(
                        passwordEncoder.matches(request.newPassword(), changePasswordUser.getPassword())).isTrue()
        );
    }

    @Test
    void 마이페이지에서_닉네임_변경_시_유저의_사용_가능_별조각이_차감되며_변경된다() {
        // given
        String nickname = "신비로운 우주의 지구";
        User user = UserFixture.createUserByNickname(nickname);
        String email = user.getEmail();
        ReflectionTestUtils.setField(user, "availableStar", 100);

        User savedUser = userRepository.save(user);

        String accessToken = tokenManager.createAccessToken(savedUser.getId(), email);
        String refreshToken = tokenManager.createRefreshToken(savedUser.getId(), email);

        NicknameChangeRequest request = new NicknameChangeRequest("변경된 유저의 아이디");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v1/me/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        SuccessResponse<Void> body = response.as(new TypeRef<>() {
        });

        User newNicknameUser = userRepository.findById(user.getId()).get();

        // then
        assertAll(
                () -> assertThat(body.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(newNicknameUser.getNickname()).isEqualTo(request.newNickname())
        );
    }

    @Test
    void 마이페이지에서_닉네임_변경_시_유저의_사용_가능_별조각이_차감되며_별조각을_사용하면_몇번이던_변경된다() {
        // given
        String nickname = "신비로운 우주의 지구";
        User user = UserFixture.createUserByNickname(nickname);
        String email = user.getEmail();
        ReflectionTestUtils.setField(user, "availableStar", 1000);

        User savedUser = userRepository.save(user);

        String accessToken = tokenManager.createAccessToken(savedUser.getId(), email);
        String refreshToken = tokenManager.createRefreshToken(savedUser.getId(), email);

        NicknameChangeRequest request = new NicknameChangeRequest("변경된 유저의 아이디");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v1/me/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        SuccessResponse<Void> body = response.as(new TypeRef<>() {
        });

        User newNicknameUser = userRepository.findById(user.getId()).get();

        // then
        assertAll(
                () -> assertThat(body.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(newNicknameUser.getNickname()).isEqualTo(request.newNickname())
        );

        NicknameChangeRequest retryRequest = new NicknameChangeRequest("또변경된 유저의 아이디");

        Response retryResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(retryRequest)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v1/me/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();

        SuccessResponse<Void> retryBody = retryResponse.as(new TypeRef<>() {
        });

        User retryNewNicknameUser = userRepository.findById(user.getId()).get();

        // then
        assertAll(
                () -> assertThat(retryBody.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(retryNewNicknameUser.getNickname()).isEqualTo(retryRequest.newNickname())
        );
    }
}
