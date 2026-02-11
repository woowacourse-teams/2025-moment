package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.application.TokenManager;
import moment.comment.dto.request.GroupCommentCreateRequest;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.dto.response.MyGroupCommentListResponse;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.like.dto.response.LikeToggleResponse;
import moment.moment.dto.request.GroupMomentCreateRequest;
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
class GroupCommentControllerTest {

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
    void 모멘트에_코멘트를_작성한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "테스트 모멘트");

        GroupCommentCreateRequest request = new GroupCommentCreateRequest("테스트 코멘트입니다", null, null);

        // when
        GroupCommentResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/comments", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupCommentResponse.class);

        // then
        assertAll(
            () -> assertThat(response.commentId()).isNotNull(),
            () -> assertThat(response.content()).isEqualTo("테스트 코멘트입니다"),
            () -> assertThat(response.memberNickname()).isEqualTo("그룹장닉네임")
        );
    }

    @Test
    void 모멘트의_코멘트_목록을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "테스트 모멘트");

        코멘트_작성(token, group.groupId(), moment.momentId(), "첫 번째 코멘트");
        코멘트_작성(token, group.groupId(), moment.momentId(), "두 번째 코멘트");

        // when
        List<GroupCommentResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments/{momentId}/comments", group.groupId(), moment.momentId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", GroupCommentResponse.class);

        // then
        assertThat(response).hasSize(2);
    }

    @Test
    void 코멘트를_삭제한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "테스트 모멘트");
        GroupCommentResponse comment = 코멘트_작성(token, group.groupId(), moment.momentId(), "삭제할 코멘트");

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().delete("/api/v2/groups/{groupId}/comments/{commentId}", group.groupId(), comment.commentId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 코멘트 목록이 비어있어야 함
        List<GroupCommentResponse> comments = RestAssured.given()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments/{momentId}/comments", group.groupId(), moment.momentId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", GroupCommentResponse.class);

        assertThat(comments).isEmpty();
    }

    @Test
    void 코멘트에_좋아요를_토글한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "테스트 모멘트");
        GroupCommentResponse comment = 코멘트_작성(token, group.groupId(), moment.momentId(), "좋아요 테스트 코멘트");

        // when - 좋아요 추가
        LikeToggleResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().post("/api/v2/groups/{groupId}/comments/{commentId}/like", group.groupId(), comment.commentId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", LikeToggleResponse.class);

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
            .jsonPath()
            .getObject("data", GroupCreateResponse.class);
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

    @Test
    void 그룹_내_나의_코멘트를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "모멘트 내용");
        코멘트_작성(token, group.groupId(), moment.momentId(), "첫 번째 코멘트");
        코멘트_작성(token, group.groupId(), moment.momentId(), "두 번째 코멘트");

        // when
        MyGroupCommentListResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/my-comments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", MyGroupCommentListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.comments()).hasSize(2),
            () -> assertThat(response.comments().get(0).moment()).isNotNull(),
            () -> assertThat(response.comments().get(0).commentNotification()).isNotNull()
        );
    }

    @Test
    void 그룹_내_읽지_않은_나의_코멘트를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "모멘트 내용");
        코멘트_작성(token, group.groupId(), moment.momentId(), "코멘트 내용");
        // 읽지 않은 알림이 없으므로 빈 응답 예상

        // when
        MyGroupCommentListResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/my-comments/unread", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", MyGroupCommentListResponse.class);

        // then
        assertThat(response.comments()).isEmpty();
    }

    @Test
    void 그룹_멤버가_아니면_나의_코멘트_조회_시_예외가_발생한다() {
        // given
        User ownerUser = UserFixture.createUser();
        User savedOwnerUser = userRepository.save(ownerUser);
        String ownerToken = tokenManager.createAccessToken(savedOwnerUser.getId(), savedOwnerUser.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        User nonMemberUser = UserFixture.createUser();
        User savedNonMemberUser = userRepository.save(nonMemberUser);
        String nonMemberToken = tokenManager.createAccessToken(savedNonMemberUser.getId(), savedNonMemberUser.getEmail());

        // when & then
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", nonMemberToken)
            .when().get("/api/v2/groups/{groupId}/my-comments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
