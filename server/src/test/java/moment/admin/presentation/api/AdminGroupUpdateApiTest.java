package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminGroupUpdateRequest;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
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
class AdminGroupUpdateApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private String sessionCookie;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        Admin admin = adminService.createAdmin("admin@test.com", "테스트관리자", "password123!@#");
        sessionCookie = 로그인("admin@test.com", "password123!@#");
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 그룹_정보_수정_성공_이름_설명_변경() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "기존그룹명", "기존설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest("새그룹명", "새설명입니다");

        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // DB 확인
        Group updatedGroup = groupRepository.findByIdIncludingDeleted(group.getId()).orElseThrow();
        assertAll(
            () -> assertThat(updatedGroup.getName()).isEqualTo("새그룹명"),
            () -> assertThat(updatedGroup.getDescription()).isEqualTo("새설명입니다")
        );
    }

    @Test
    void 그룹_정보_수정_삭제된_그룹_수정시_400_AG003() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // 그룹 삭제 (Soft Delete)
        groupRepository.delete(group);

        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest("새그룹명", "새설명");

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("AG-003");
    }

    @Test
    void 그룹_정보_수정_이름_누락시_400() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트", "설명"));

        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest(null, "새설명");

        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 그룹_정보_수정_설명_누락시_400() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트", "설명"));

        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest("새그룹명", null);

        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 그룹_정보_수정_이름_30자_초과시_400() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트", "설명"));

        String longName = "가".repeat(31);
        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest(longName, "새설명");

        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 그룹_정보_수정_그룹없으면_404() {
        // given
        AdminGroupUpdateRequest request = new AdminGroupUpdateRequest("새그룹명", "새설명");

        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .contentType(ContentType.JSON)
            .body(request)
            .when().put("/api/admin/groups/{groupId}", 999999L)
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private String 로그인(String email, String password) {
        AdminLoginRequest loginRequest = new AdminLoginRequest(email, password);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when().post("/api/admin/auth/login")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .cookie("SESSION");
    }
}
