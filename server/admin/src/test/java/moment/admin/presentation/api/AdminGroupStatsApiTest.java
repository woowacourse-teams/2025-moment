package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminGroupStatsResponse;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.GroupMemberFixture;
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
class AdminGroupStatsApiTest {

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

        // Admin 생성 및 로그인
        Admin admin = adminService.createAdmin("admin@test.com", "테스트관리자", "password123!@#");
        sessionCookie = 로그인("admin@test.com", "password123!@#");
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 그룹_통계_조회_성공_전체_그룹_수_반환() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group1 = groupRepository.save(GroupFixture.createGroup(owner));
        Group group2 = groupRepository.save(GroupFixture.createGroup(owner));

        groupMemberRepository.save(GroupMember.createOwner(group1, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(group2, owner, "닉네임2"));

        // when
        AdminGroupStatsResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/stats")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupStatsResponse.class);

        // then
        assertThat(response.totalGroups()).isEqualTo(2);
    }

    @Test
    void 그룹_통계_조회_성공_활성_삭제_그룹_수_분리() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group activeGroup = groupRepository.save(GroupFixture.createGroup(owner));
        Group deletedGroup = groupRepository.save(GroupFixture.createGroup(owner));

        groupMemberRepository.save(GroupMember.createOwner(activeGroup, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(deletedGroup, owner, "닉네임2"));

        // 그룹 삭제 (Soft Delete)
        groupRepository.delete(deletedGroup);

        // when
        AdminGroupStatsResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/stats")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupStatsResponse.class);

        // then
        assertAll(
            () -> assertThat(response.totalGroups()).isEqualTo(2),
            () -> assertThat(response.activeGroups()).isEqualTo(1),
            () -> assertThat(response.deletedGroups()).isEqualTo(1)
        );
    }

    @Test
    void 그룹_통계_조회_성공_전체_멤버_수_집계() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User member1 = userRepository.save(UserFixture.createUser());
        User member2 = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroup(owner));

        // owner (APPROVED) + 2 members (APPROVED)
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember m1 = GroupMember.createPendingMember(group, member1, "멤버1");
        m1.approve();
        groupMemberRepository.save(m1);
        GroupMember m2 = GroupMember.createPendingMember(group, member2, "멤버2");
        m2.approve();
        groupMemberRepository.save(m2);

        // when
        AdminGroupStatsResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/stats")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupStatsResponse.class);

        // then
        assertThat(response.totalMembers()).isEqualTo(3);
    }

    @Test
    void 그룹_통계_조회_인증없이_접근시_401() {
        // when & then
        RestAssured.given().log().all()
            .when().get("/api/admin/groups/stats")
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
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
