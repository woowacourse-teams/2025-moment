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
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
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
class AdminMemberKickApiTest {

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

    @Nested
    class 멤버_강제추방 {

        @Test
        void 멤버_강제추방_성공_상태변경_및_SoftDelete() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User memberUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, memberUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            // then - SQLRestriction으로 인해 삭제된 멤버는 조회되지 않음
            assertThat(groupMemberRepository.findById(approvedMember.getId())).isEmpty();
        }

        @Test
        void 멤버_강제추방_Owner_추방시_400_AM002() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), ownerMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 멤버_강제추방_APPROVED_아닌_멤버_추방시_400() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pendingUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember pendingMember = groupMemberRepository.save(GroupMember.createPendingMember(group, pendingUser, "대기멤버"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), pendingMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 멤버_강제추방_성공_해당멤버_모멘트_SoftDelete() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User memberUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, memberUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);

            // 모멘트 생성
            Moment moment1 = momentRepository.save(new Moment(memberUser, group, approvedMember, "내용1"));
            Moment moment2 = momentRepository.save(new Moment(memberUser, group, approvedMember, "내용2"));

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            // then - SQLRestriction으로 인해 삭제된 모멘트는 조회되지 않음
            assertThat(momentRepository.findById(moment1.getId())).isEmpty();
            assertThat(momentRepository.findById(moment2.getId())).isEmpty();
        }

        @Test
        void 멤버_강제추방_성공_해당멤버_코멘트_SoftDelete() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User memberUser = userRepository.save(UserFixture.createUser());
            User otherUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, memberUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);
            GroupMember otherMember = GroupMember.createPendingMember(group, otherUser, "다른멤버");
            otherMember.approve();
            GroupMember approvedOtherMember = groupMemberRepository.save(otherMember);

            // 다른 멤버의 모멘트에 추방될 멤버가 코멘트 작성
            Moment otherMoment = momentRepository.save(new Moment(otherUser, group, approvedOtherMember, "다른사람모멘트"));
            Comment comment = commentRepository.save(new Comment(otherMoment, memberUser, approvedMember, "코멘트내용"));

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            // then - SQLRestriction으로 인해 삭제된 코멘트는 조회되지 않음
            assertThat(commentRepository.findById(comment.getId())).isEmpty();
        }

        @Test
        void 멤버_강제추방_멤버없으면_404_AM001() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", group.getId(), 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 멤버_강제추방_그룹없으면_404_AG001() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().delete("/api/admin/groups/{groupId}/members/{memberId}", 999999L, 1L)
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
