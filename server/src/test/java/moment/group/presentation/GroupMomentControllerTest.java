package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.like.dto.response.LikeToggleResponse;
import moment.moment.dto.request.GroupMomentCreateRequest;
import moment.moment.dto.response.GroupFeedResponse;
import moment.moment.dto.response.GroupMomentResponse;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupMomentControllerTest {

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
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 그룹에_모멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        GroupMomentCreateRequest request = new GroupMomentCreateRequest("오늘의 모멘트입니다");

        // when
        GroupMomentResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(GroupMomentResponse.class);

        // then
        assertAll(
            () -> assertThat(response.momentId()).isNotNull(),
            () -> assertThat(response.content()).isEqualTo("오늘의 모멘트입니다"),
            () -> assertThat(response.memberNickname()).isEqualTo("그룹장닉네임")
        );
    }

    @Test
    void 그룹_피드를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        // 모멘트 2개 작성
        모멘트_작성(token, group.groupId(), "첫 번째 모멘트");
        모멘트_작성(token, group.groupId(), "두 번째 모멘트");

        // when
        GroupFeedResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(GroupFeedResponse.class);

        // then
        assertThat(response.moments()).hasSize(2);
    }

    @Test
    void 나의_모음집을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        // 모멘트 작성
        모멘트_작성(token, group.groupId(), "내 모멘트");

        // when
        GroupFeedResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/my-moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(GroupFeedResponse.class);

        // then
        assertAll(
            () -> assertThat(response.moments()).hasSize(1),
            () -> assertThat(response.moments().get(0).content()).isEqualTo("내 모멘트")
        );
    }

    @Test
    void 모멘트를_삭제한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "삭제할 모멘트");

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().delete("/api/v2/groups/{groupId}/moments/{momentId}", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 피드가 비어있어야 함
        GroupFeedResponse feed = RestAssured.given()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then()
            .extract()
            .as(GroupFeedResponse.class);

        assertThat(feed.moments()).isEmpty();
    }

    @Test
    void 모멘트에_좋아요를_토글한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "좋아요 테스트");

        // when - 좋아요 추가
        LikeToggleResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/like", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(LikeToggleResponse.class);

        // then
        assertAll(
            () -> assertThat(response.liked()).isTrue(),
            () -> assertThat(response.likeCount()).isEqualTo(1)
        );
    }

    private GroupCreateResponse 그룹_생성(String token, String name, String description, String nickname) {
        GroupCreateRequest request = new GroupCreateRequest(name, description, nickname);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(GroupCreateResponse.class);
    }

    private GroupMomentResponse 모멘트_작성(String token, Long groupId, String content) {
        GroupMomentCreateRequest request = new GroupMomentCreateRequest(content);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments", groupId)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(GroupMomentResponse.class);
    }
}
