package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.service.auth.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.domain.MemberStatus;
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
class GroupInviteControllerTest {

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
    void 초대_링크를_생성한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // when
        String inviteCode = RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().post("/api/v2/groups/{groupId}/invite", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getString("data");

        // then
        assertThat(inviteCode).isNotBlank();
    }

    @Test
    void 초대_정보를_조회한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");
        String inviteCode = group.inviteCode();

        // when
        InviteInfoResponse response = RestAssured.given().log().all()
            .when().get("/api/v2/invite/{code}", inviteCode)
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", InviteInfoResponse.class);

        // then
        assertAll(
            () -> assertThat(response.groupId()).isEqualTo(group.groupId()),
            () -> assertThat(response.name()).isEqualTo("테스트 그룹"),
            () -> assertThat(response.description()).isEqualTo("설명"),
            () -> assertThat(response.memberCount()).isEqualTo(1)
        );
    }

    @Test
    void 그룹에_가입_신청한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        User newMember = UserFixture.createUserByEmail("newmember@test.com");
        User savedNewMember = userRepository.save(newMember);
        String newMemberToken = tokenManager.createAccessToken(savedNewMember.getId(), savedNewMember.getEmail());

        GroupJoinRequest joinRequest = new GroupJoinRequest(group.inviteCode(), "새멤버닉네임");

        // when
        GroupJoinResponse response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", newMemberToken)
            .body(joinRequest)
            .when().post("/api/v2/groups/join")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupJoinResponse.class);

        // then
        assertAll(
            () -> assertThat(response.memberId()).isNotNull(),
            () -> assertThat(response.groupId()).isEqualTo(group.groupId()),
            () -> assertThat(response.nickname()).isEqualTo("새멤버닉네임"),
            () -> assertThat(response.status()).isEqualTo(MemberStatus.PENDING)
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
}
