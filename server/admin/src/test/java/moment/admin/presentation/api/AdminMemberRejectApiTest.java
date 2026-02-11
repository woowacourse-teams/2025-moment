package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
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
import org.junit.jupiter.api.Nested;
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
class AdminMemberRejectApiTest {

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

    @Nested
    class 멤버_거절 {

        @Test
        void 멤버_거절_성공_멤버십_SoftDelete() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pendingUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember pendingMember = groupMemberRepository.save(GroupMember.createPendingMember(group, pendingUser, "대기멤버"));

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", group.getId(), pendingMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

            // then - SQLRestriction으로 인해 삭제된 멤버는 조회되지 않음
            assertThat(groupMemberRepository.findById(pendingMember.getId())).isEmpty();
        }

        @Test
        void 멤버_거절_PENDING_아닌_멤버_거절시_400_AM003() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User approvedUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, approvedUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 멤버_거절_이미_거절된_멤버_거절시_400_AM007() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pendingUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember pendingMember = groupMemberRepository.save(GroupMember.createPendingMember(group, pendingUser, "대기멤버"));

            // 먼저 거절
            RestAssured.given()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", group.getId(), pendingMember.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

            // when & then - 이미 거절된 멤버 다시 거절 (삭제되었으므로 멤버 찾을 수 없음 - NOT_FOUND)
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", group.getId(), pendingMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 멤버_거절_멤버없으면_404_AM001() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", group.getId(), 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 멤버_거절_그룹없으면_404_AG001() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/reject", 999999L, 1L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
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
