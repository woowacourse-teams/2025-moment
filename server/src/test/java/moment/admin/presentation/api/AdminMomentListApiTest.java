package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminMomentListResponse;
import moment.admin.service.admin.AdminService;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.CommentFixture;
import moment.fixture.GroupFixture;
import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
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
class AdminMomentListApiTest {

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

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

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
    void 모멘트_목록_조회_성공_그룹의_모멘트_반환() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(owner, group, ownerMember));

        // when
        AdminMomentListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}/moments", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminMomentListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(1),
            () -> assertThat(response.content().get(0).momentId()).isEqualTo(moment.getId()),
            () -> assertThat(response.content().get(0).author().memberId()).isEqualTo(ownerMember.getId())
        );
    }

    @Test
    void 모멘트_목록_조회_성공_페이지네이션_적용() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        // 15개 모멘트 생성
        for (int i = 0; i < 15; i++) {
            momentRepository.save(MomentFixture.createMomentInGroup(owner, group, ownerMember));
        }

        // when
        AdminMomentListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when().get("/api/admin/groups/{groupId}/moments", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminMomentListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(10),
            () -> assertThat(response.page()).isEqualTo(0),
            () -> assertThat(response.size()).isEqualTo(10),
            () -> assertThat(response.totalElements()).isEqualTo(15),
            () -> assertThat(response.totalPages()).isEqualTo(2)
        );
    }

    @Test
    void 모멘트_목록_조회_성공_코멘트_수_포함() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User member = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember approvedMember = GroupMember.createPendingMember(group, member, "멤버");
        approvedMember.approve();
        groupMemberRepository.save(approvedMember);

        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(owner, group, ownerMember));

        // 코멘트 3개 추가
        commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));
        commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));
        commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));

        // when
        AdminMomentListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}/moments", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminMomentListResponse.class);

        // then
        assertThat(response.content().get(0).commentCount()).isEqualTo(3);
    }

    @Test
    void 모멘트_목록_조회_그룹없으면_404() {
        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}/moments", 999999L)
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
