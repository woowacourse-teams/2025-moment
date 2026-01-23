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
import moment.user.domain.User;
import moment.user.dto.request.ChangePasswordRequest;
import moment.user.dto.request.NicknameChangeRequest;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.NicknameChangeResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
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
                .when().get("/api/v2/me/profile")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        MyPageProfileResponse profile = response.data();
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(profile.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(profile.email()).isEqualTo(user.getEmail())
        );
    }

    @Test
    void 마이페이지에서_유저_닉네임을_변경한다() {
        // given
        User user = UserFixture.createUserByEmailAndNickname("test@gmail.com", "신비로운 행성의 지구");
        User savedUser = userRepository.save(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        NicknameChangeRequest request = new NicknameChangeRequest("변경될 유저의 닉네임");

        // when
        SuccessResponse<NicknameChangeResponse> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", token)
                .when().post("/api/v2/me/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        User nicknameChangedUser = userRepository.findById(savedUser.getId()).get();

        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(nicknameChangedUser.getNickname()).isEqualTo(request.newNickname())
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
                .when().post("/api/v2/me/password")
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
    void 마이페이지에서_닉네임을_변경한다() {
        // given
        String nickname = "신비로운 우주의 지구";
        User user = UserFixture.createUserByNickname(nickname);
        String email = user.getEmail();

        User savedUser = userRepository.save(user);

        String accessToken = tokenManager.createAccessToken(savedUser.getId(), email);
        String refreshToken = tokenManager.createRefreshToken(savedUser.getId(), email);

        NicknameChangeRequest request = new NicknameChangeRequest("변경된 유저의 아이디");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v2/me/nickname")
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
    void 마이페이지에서_닉네임을_여러번_변경할_수_있다() {
        // given
        String nickname = "신비로운 우주의 지구";
        User user = UserFixture.createUserByNickname(nickname);
        String email = user.getEmail();

        User savedUser = userRepository.save(user);

        String accessToken = tokenManager.createAccessToken(savedUser.getId(), email);
        String refreshToken = tokenManager.createRefreshToken(savedUser.getId(), email);

        NicknameChangeRequest request = new NicknameChangeRequest("변경된 유저의 아이디");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .when().post("/api/v2/me/nickname")
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
                .when().post("/api/v2/me/nickname")
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
