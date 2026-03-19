package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.service.auth.TokenManager;
import moment.comment.dto.request.GroupCommentCreateRequest;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.like.dto.response.LikeToggleResponse;
import moment.moment.dto.request.GroupMomentCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.GroupMomentListResponse;
import moment.moment.dto.response.GroupMomentResponse;
import moment.moment.dto.response.MyGroupMomentListResponse;
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

        GroupMomentCreateRequest request = new GroupMomentCreateRequest("오늘의 모멘트입니다", null, null);

        // when
        GroupMomentResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentResponse.class);

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
        GroupMomentListResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentListResponse.class);

        // then
        assertThat(response.moments()).hasSize(2);
    }

    @Test
    void 그룹_내_나의_모멘트를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        // 모멘트 작성
        모멘트_작성(token, group.groupId(), "내 모멘트");

        // when
        MyGroupMomentListResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/my-moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", MyGroupMomentListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.moments()).hasSize(1),
            () -> assertThat(response.moments().get(0).content()).isEqualTo("내 모멘트"),
            () -> assertThat(response.moments().get(0).comments()).isNotNull(),
            () -> assertThat(response.moments().get(0).momentNotification()).isNotNull()
        );
    }

    @Test
    void 그룹_내_읽지_않은_나의_모멘트를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        // 모멘트 작성 (알림 없으므로 unread도 없음)
        모멘트_작성(token, group.groupId(), "내 모멘트");

        // when
        MyGroupMomentListResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/my-moments/unread", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", MyGroupMomentListResponse.class);

        // then - 알림이 없으므로 빈 응답
        assertThat(response.moments()).isEmpty();
    }

    @Test
    void 그룹_멤버가_아니면_나의_모멘트_조회_시_예외가_발생한다() {
        // given
        User groupOwner = UserFixture.createUser();
        User savedOwner = userRepository.save(groupOwner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        User nonMember = UserFixture.createUser();
        User savedNonMember = userRepository.save(nonMember);
        String nonMemberToken = tokenManager.createAccessToken(savedNonMember.getId(), savedNonMember.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // when & then
        RestAssured.given().log().all()
            .cookie("accessToken", nonMemberToken)
            .when().get("/api/v2/groups/{groupId}/my-moments", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value());
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
        GroupMomentListResponse feed = RestAssured.given()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments", group.groupId())
            .then()
            .extract()
            .jsonPath()
            .getObject("data", GroupMomentListResponse.class);

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

    @Test
    void 그룹_내_코멘트를_작성할_수_있는_모멘트를_조회한다() {
        // given
        User user1 = UserFixture.createUser();
        User savedUser1 = userRepository.save(user1);
        String token1 = tokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

        User user2 = UserFixture.createUser();
        User savedUser2 = userRepository.save(user2);
        String token2 = tokenManager.createAccessToken(savedUser2.getId(), savedUser2.getEmail());

        // 그룹 생성 (user1이 그룹장)
        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "그룹장닉네임");

        // user2를 그룹에 초대 (초대 코드 사용)
        그룹_가입(token2, group.inviteCode(), "멤버닉네임");

        // user1이 모멘트 작성
        모멘트_작성(token1, group.groupId(), "코멘트 가능한 모멘트");

        // when - user2가 코멘트 가능한 모멘트 조회
        CommentableMomentResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token2)
            .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", CommentableMomentResponse.class);

        // then
        assertAll(
            () -> assertThat(response.id()).isNotNull(),
            () -> assertThat(response.content()).isEqualTo("코멘트 가능한 모멘트")
        );
    }

    @Test
    void 그룹_멤버가_아닌_경우_코멘트_가능_모멘트_조회_실패() {
        // given
        User groupOwner = UserFixture.createUser();
        User savedOwner = userRepository.save(groupOwner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        User nonMember = UserFixture.createUser();
        User savedNonMember = userRepository.save(nonMember);
        String nonMemberToken = tokenManager.createAccessToken(savedNonMember.getId(), savedNonMember.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // when & then
        RestAssured.given().log().all()
            .cookie("accessToken", nonMemberToken)
            .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 본인_모멘트는_코멘트_가능_모멘트에서_제외된다() {
        // given - 그룹에 혼자만 있고, 본인 모멘트만 있는 경우
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
        모멘트_작성(token, group.groupId(), "내가 작성한 모멘트");

        // when
        CommentableMomentResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", CommentableMomentResponse.class);

        // then - null 응답 (본인 모멘트 제외)
        assertThat(response).isNull();
    }

    @Test
    void 이미_코멘트한_모멘트가_있어도_다른_코멘트_가능한_모멘트를_반환한다() {
        // given
        User user1 = UserFixture.createUser();
        User savedUser1 = userRepository.save(user1);
        String token1 = tokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

        User user2 = UserFixture.createUser();
        User savedUser2 = userRepository.save(user2);
        String token2 = tokenManager.createAccessToken(savedUser2.getId(), savedUser2.getEmail());

        // 그룹 생성 (user1이 그룹장)
        GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "그룹장닉네임");

        // user2를 그룹에 가입
        그룹_가입(token2, group.inviteCode(), "멤버닉네임");

        // user1이 모멘트 2개 작성
        GroupMomentResponse moment1 = 모멘트_작성(token1, group.groupId(), "첫 번째 모멘트");
        모멘트_작성(token1, group.groupId(), "두 번째 모멘트");

        // user2가 첫 번째 모멘트에 코멘트 작성
        코멘트_작성(token2, group.groupId(), moment1.momentId(), "코멘트입니다");

        // when - user2가 코멘트 가능한 모멘트 조회
        CommentableMomentResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token2)
            .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", CommentableMomentResponse.class);

        // then - 코멘트 안 단 두 번째 모멘트가 반환되어야 함 (null이 아님)
        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.content()).isEqualTo("두 번째 모멘트")
        );
    }

    @Test
    void 다른_그룹의_모멘트는_조회되지_않는다() {
        // given
        User user1 = UserFixture.createUser();
        User savedUser1 = userRepository.save(user1);
        String token1 = tokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

        User user2 = UserFixture.createUser();
        User savedUser2 = userRepository.save(user2);
        String token2 = tokenManager.createAccessToken(savedUser2.getId(), savedUser2.getEmail());

        // 그룹 A 생성 (user1)
        GroupCreateResponse groupA = 그룹_생성(token1, "그룹 A", "설명", "닉네임A");

        // 그룹 B 생성 (user2)
        GroupCreateResponse groupB = 그룹_생성(token2, "그룹 B", "설명", "닉네임B");

        // user2가 그룹 B에 모멘트 작성
        모멘트_작성(token2, groupB.groupId(), "그룹 B의 모멘트");

        // when - user1이 그룹 A에서 코멘트 가능 모멘트 조회
        CommentableMomentResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token1)
            .when().get("/api/v2/groups/{groupId}/moments/commentable", groupA.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", CommentableMomentResponse.class);

        // then - 그룹 A에는 모멘트가 없으므로 null
        assertThat(response).isNull();
    }

    private void 코멘트_작성(String token, Long groupId, Long momentId, String content) {
        GroupCommentCreateRequest request = new GroupCommentCreateRequest(content, null, null);
        RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups/{groupId}/moments/{momentId}/comments", groupId, momentId)
            .then()
            .statusCode(HttpStatus.CREATED.value());
    }
}
