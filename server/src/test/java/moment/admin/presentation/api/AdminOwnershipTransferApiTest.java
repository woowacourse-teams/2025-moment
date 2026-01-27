package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import moment.group.domain.MemberRole;
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
class AdminOwnershipTransferApiTest {

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
    class 소유권_이전 {

        @Test
        void 소유권_이전_성공_기존Owner_MEMBER로_새멤버_OWNER로() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User memberUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, memberUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

            // then
            GroupMember previousOwner = groupMemberRepository.findById(ownerMember.getId()).orElseThrow();
            GroupMember newOwner = groupMemberRepository.findById(approvedMember.getId()).orElseThrow();
            assertAll(
                () -> assertThat(previousOwner.getRole()).isEqualTo(MemberRole.MEMBER),
                () -> assertThat(newOwner.getRole()).isEqualTo(MemberRole.OWNER)
            );
        }

        @Test
        void 소유권_이전_APPROVED_아닌_멤버에게_이전시_400_AM004() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pendingUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember pendingMember = groupMemberRepository.save(GroupMember.createPendingMember(group, pendingUser, "대기멤버"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}", group.getId(), pendingMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 소유권_이전_이미_OWNER인_멤버에게_이전시_400_AM005() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}", group.getId(), ownerMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 소유권_이전_멤버없으면_404_AM001() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}", group.getId(), 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 소유권_이전_그룹없으면_404_AG001() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}", 999999L, 1L)
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
