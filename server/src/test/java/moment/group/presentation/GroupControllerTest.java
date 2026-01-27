package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import java.util.List;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupUpdateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
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
class GroupControllerTest {

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
    void 그룹을_생성한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateRequest request = new GroupCreateRequest("테스트 그룹", "그룹 설명입니다", "그룹장닉네임");

        // when
        GroupCreateResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupCreateResponse.class);

        // then
        assertAll(
            () -> assertThat(response.groupId()).isNotNull(),
            () -> assertThat(response.name()).isEqualTo("테스트 그룹"),
            () -> assertThat(response.description()).isEqualTo("그룹 설명입니다"),
            () -> assertThat(response.nickname()).isEqualTo("그룹장닉네임"),
            () -> assertThat(response.inviteCode()).isNotBlank()
        );
    }

    @Test
    void 내_그룹_목록을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        // 그룹 2개 생성
        그룹_생성(token, "그룹1", "설명1", "닉네임1");
        그룹_생성(token, "그룹2", "설명2", "닉네임2");

        // when
        List<MyGroupResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", MyGroupResponse.class);

        // then
        assertThat(response).hasSize(2);
    }

    @Test
    void 그룹_상세를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse createdGroup = 그룹_생성(token, "테스트 그룹", "그룹 설명", "그룹장닉네임");

        // when
        GroupDetailResponse response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}", createdGroup.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupDetailResponse.class);

        // then
        assertAll(
            () -> assertThat(response.groupId()).isEqualTo(createdGroup.groupId()),
            () -> assertThat(response.name()).isEqualTo("테스트 그룹"),
            () -> assertThat(response.description()).isEqualTo("그룹 설명"),
            () -> assertThat(response.myNickname()).isEqualTo("그룹장닉네임"),
            () -> assertThat(response.isOwner()).isTrue(),
            () -> assertThat(response.memberCount()).isEqualTo(1)
        );
    }

    @Test
    void 그룹을_수정한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse createdGroup = 그룹_생성(token, "원래 이름", "원래 설명", "그룹장닉네임");

        GroupUpdateRequest updateRequest = new GroupUpdateRequest("변경된 이름", "변경된 설명");

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(updateRequest)
            .when().patch("/api/v2/groups/{groupId}", createdGroup.groupId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        GroupDetailResponse detail = RestAssured.given()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}", createdGroup.groupId())
            .then()
            .extract()
            .jsonPath()
            .getObject("data", GroupDetailResponse.class);

        assertAll(
            () -> assertThat(detail.name()).isEqualTo("변경된 이름"),
            () -> assertThat(detail.description()).isEqualTo("변경된 설명")
        );
    }

    @Test
    void 그룹을_삭제한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse createdGroup = 그룹_생성(token, "삭제할 그룹", "설명", "그룹장닉네임");

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().delete("/api/v2/groups/{groupId}", createdGroup.groupId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 그룹 목록에서 삭제된 그룹이 없어야 함
        List<MyGroupResponse> myGroups = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", MyGroupResponse.class);

        assertThat(myGroups).isEmpty();
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
}
