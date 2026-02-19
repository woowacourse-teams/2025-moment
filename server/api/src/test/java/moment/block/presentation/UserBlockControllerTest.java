package moment.block.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.service.auth.TokenManager;
import moment.block.dto.response.UserBlockListResponse;
import moment.block.dto.response.UserBlockResponse;
import moment.comment.dto.request.GroupCommentCreateRequest;
import moment.comment.dto.response.GroupCommentResponse;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.like.dto.response.LikeToggleResponse;
import moment.moment.dto.request.GroupMomentCreateRequest;
import moment.moment.dto.response.GroupMomentListResponse;
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
class UserBlockControllerTest {

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
    void 사용자_차단_성공() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());

        // when
        UserBlockResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().post("/api/v2/users/{userId}/blocks", user2.getId())
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", UserBlockResponse.class);

        // then
        assertAll(
            () -> assertThat(response.blockedUserId()).isEqualTo(user2.getId()),
            () -> assertThat(response.createdAt()).isNotNull()
        );
    }

    @Test
    void 자기_자신_차단_실패() {
        // given
        User user = userRepository.save(UserFixture.createUser());
        String token = tokenManager.createAccessToken(user.getId(), user.getEmail());

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().post("/api/v2/users/{userId}/blocks", user.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("BL-001");
    }

    @Test
    void 이미_차단된_사용자_차단_실패() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());

        사용자_차단(token1, user2.getId());

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().post("/api/v2/users/{userId}/blocks", user2.getId())
            .then().log().all()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("BL-002");
    }

    @Test
    void 차단_해제_성공() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());

        사용자_차단(token1, user2.getId());

        // when & then
        RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().delete("/api/v2/users/{userId}/blocks", user2.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 존재하지_않는_차단_해제_실패() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().delete("/api/v2/users/{userId}/blocks", user2.getId())
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("BL-003");
    }

    @Test
    void 차단_목록_조회_성공() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        User user3 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());

        사용자_차단(token1, user2.getId());
        사용자_차단(token1, user3.getId());

        // when
        List<UserBlockListResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().get("/api/v2/users/blocks")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", UserBlockListResponse.class);

        // then
        assertThat(response).hasSize(2);
    }

    @Test
    void 차단_후_그룹_피드에서_모멘트_미노출() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());
        String token2 = tokenManager.createAccessToken(user2.getId(), user2.getEmail());

        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "유저1닉네임");
        그룹_가입(token2, group.inviteCode(), "유저2닉네임");

        모멘트_작성(token1, group.groupId(), "유저1의 모멘트");
        모멘트_작성(token2, group.groupId(), "유저2의 모멘트");

        // user1이 user2를 차단
        사용자_차단(token1, user2.getId());

        // when - user1이 그룹 피드 조회
        GroupMomentListResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentListResponse.class);

        // then - user2의 모멘트가 필터링되어 user1의 모멘트만 노출
        assertAll(
            () -> assertThat(response.moments()).hasSize(1),
            () -> assertThat(response.moments().get(0).content()).isEqualTo("유저1의 모멘트")
        );
    }

    @Test
    void 차단_후_댓글_미노출() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());
        String token2 = tokenManager.createAccessToken(user2.getId(), user2.getEmail());

        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "유저1닉네임");
        그룹_가입(token2, group.inviteCode(), "유저2닉네임");

        GroupMomentResponse moment = 모멘트_작성(token1, group.groupId(), "유저1의 모멘트");
        코멘트_작성(token1, group.groupId(), moment.momentId(), "유저1의 댓글");
        코멘트_작성(token2, group.groupId(), moment.momentId(), "유저2의 댓글");

        // user1이 user2를 차단
        사용자_차단(token1, user2.getId());

        // when - user1이 댓글 목록 조회
        List<GroupCommentResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().get("/api/v2/groups/{groupId}/moments/{momentId}/comments", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", GroupCommentResponse.class);

        // then - user2의 댓글이 필터링되어 user1의 댓글만 노출
        assertAll(
            () -> assertThat(response).hasSize(1),
            () -> assertThat(response.get(0).content()).isEqualTo("유저1의 댓글")
        );
    }

    @Test
    void 차단된_사용자_모멘트에_댓글_작성_실패() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());
        String token2 = tokenManager.createAccessToken(user2.getId(), user2.getEmail());

        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "유저1닉네임");
        그룹_가입(token2, group.inviteCode(), "유저2닉네임");

        GroupMomentResponse moment = 모멘트_작성(token1, group.groupId(), "유저1의 모멘트");

        // user2가 user1을 차단
        사용자_차단(token2, user1.getId());

        // when - user2가 user1의 모멘트에 댓글 작성 시도
        GroupCommentCreateRequest request = new GroupCommentCreateRequest("차단된 상태의 댓글", null, null);

        String errorCode = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token2)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/comments", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract()
            .jsonPath()
            .getString("code");

        // then
        assertThat(errorCode).isEqualTo("BL-004");
    }

    @Test
    void 차단된_사용자_모멘트에_좋아요_실패() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());
        String token2 = tokenManager.createAccessToken(user2.getId(), user2.getEmail());

        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "유저1닉네임");
        그룹_가입(token2, group.inviteCode(), "유저2닉네임");

        GroupMomentResponse moment = 모멘트_작성(token1, group.groupId(), "유저1의 모멘트");

        // user2가 user1을 차단
        사용자_차단(token2, user1.getId());

        // when & then - user2가 user1의 모멘트에 좋아요 시도
        String errorCode = RestAssured.given().log().all()
            .cookie("accessToken", token2)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/like", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("BL-004");
    }

    @Test
    void 차단_해제_후_콘텐츠_정상_노출() {
        // given
        User user1 = userRepository.save(UserFixture.createUser());
        User user2 = userRepository.save(UserFixture.createUser());
        String token1 = tokenManager.createAccessToken(user1.getId(), user1.getEmail());
        String token2 = tokenManager.createAccessToken(user2.getId(), user2.getEmail());

        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "유저1닉네임");
        그룹_가입(token2, group.inviteCode(), "유저2닉네임");

        모멘트_작성(token1, group.groupId(), "유저1의 모멘트");
        모멘트_작성(token2, group.groupId(), "유저2의 모멘트");

        // user1이 user2를 차단 후 해제
        사용자_차단(token1, user2.getId());
        사용자_차단_해제(token1, user2.getId());

        // when - user1이 그룹 피드 조회
        GroupMomentListResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentListResponse.class);

        // then - 차단 해제 후 두 모멘트 모두 노출
        assertThat(response.moments()).hasSize(2);
    }

    // --- Helper Methods ---

    private void 사용자_차단(String token, Long targetUserId) {
        RestAssured.given()
            .cookie("accessToken", token)
            .when().post("/api/v2/users/{userId}/blocks", targetUserId)
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }

    private void 사용자_차단_해제(String token, Long targetUserId) {
        RestAssured.given()
            .cookie("accessToken", token)
            .when().delete("/api/v2/users/{userId}/blocks", targetUserId)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
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
            .jsonPath()
            .getObject("data", GroupCreateResponse.class);
    }

    private void 그룹_가입(String token, String inviteCode, String nickname) {
        GroupJoinRequest request = new GroupJoinRequest(inviteCode, nickname);
        RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/join")
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    private GroupMomentResponse 모멘트_작성(String token, Long groupId, String content) {
        GroupMomentCreateRequest request = new GroupMomentCreateRequest(content, null, null);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments", groupId)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentResponse.class);
    }

    private GroupCommentResponse 코멘트_작성(String token, Long groupId, Long momentId, String content) {
        GroupCommentCreateRequest request = new GroupCommentCreateRequest(content, null, null);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/comments", groupId, momentId)
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupCommentResponse.class);
    }
}
