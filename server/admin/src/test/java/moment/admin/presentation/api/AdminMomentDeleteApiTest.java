package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
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
class AdminMomentDeleteApiTest {

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
    void 모멘트_삭제_성공_SoftDelete() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        Moment moment = momentRepository.save(MomentFixture.createMomentInGroup(owner, group, ownerMember));
        Long momentId = moment.getId();

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}/moments/{momentId}", group.getId(), momentId)
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - Soft Delete 확인 (일반 조회에서는 안 보임)
        assertThat(momentRepository.findById(momentId)).isEmpty();
    }

    @Test
    void 모멘트_삭제_성공_해당_모멘트_코멘트_전체_SoftDelete() {
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
        Comment c1 = commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));
        Comment c2 = commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));
        Comment c3 = commentRepository.save(CommentFixture.createCommentInGroup(moment, member, approvedMember));

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}/moments/{momentId}", group.getId(), moment.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 코멘트들도 Soft Delete됨
        assertThat(commentRepository.findById(c1.getId())).isEmpty();
        assertThat(commentRepository.findById(c2.getId())).isEmpty();
        assertThat(commentRepository.findById(c3.getId())).isEmpty();
    }

    @Test
    void 모멘트_삭제_모멘트없으면_404_AC001() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}/moments/{momentId}", group.getId(), 999999L)
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("AC-001");
    }

    @Test
    void 모멘트_삭제_그룹없으면_404() {
        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}/moments/{momentId}", 999999L, 1L)
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
