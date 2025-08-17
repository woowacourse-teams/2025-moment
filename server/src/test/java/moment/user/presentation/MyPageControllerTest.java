package moment.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import moment.auth.application.TokenManager;
import moment.global.dto.response.SuccessResponse;
import moment.reward.domain.Reason;
import moment.reward.domain.RewardHistory;
import moment.reward.infrastructure.RewardRepository;
import moment.user.domain.Level;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.dto.response.MyRewardHistoryPageResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MyPageControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 유저_프로필_정보를_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);
        userRepository.save(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // when
        SuccessResponse<MyPageProfileResponse> response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .when().get("/api/v1/my/profile")
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
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);
        userRepository.save(user);

        createTestRewardHistory(user);

        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // when
        SuccessResponse<MyRewardHistoryPageResponse> response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .param("pageNum", 1)
                .param("pageSize", 10)
                .when().get("/api/v1/my/reward/history")
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
                    new RewardHistory(user, Reason.MOMENT_CREATION.getPointTo(), Reason.MOMENT_CREATION, (long) i));
        }
    }
}
