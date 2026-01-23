package moment.comment.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import moment.auth.infrastructure.JwtTokenManager;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.request.CommentReportCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.CommentReportCreateResponse;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.dto.response.MyCommentResponse;
import moment.comment.infrastructure.CommentImageRepository;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.support.CommentCreatedAtHelper;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@org.junit.jupiter.api.Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class CommentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private CommentCreatedAtHelper commentCreatedAtHelper;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void Comment_생성에_성공한다() {
        // given
        User user1 = UserFixture.createUser();
        User savedUser1 = userRepository.saveAndFlush(user1);

        User user2 = UserFixture.createUser();
        User savedUser2 = userRepository.saveAndFlush(user2);

        Moment moment = new Moment("개발의 세계는 신비해요!", user2);
        momentRepository.saveAndFlush(moment);

        String token = jwtTokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

        CommentCreateRequest request = new CommentCreateRequest("정말 안타깝게 됐네요!", 1L, null, null);

        CommentCreateResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("/api/v2/comments")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", CommentCreateResponse.class);

        // then
        assertThatCode(
                () -> commentRepository.findById(response.commentId())
                        .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND)))
                .doesNotThrowAnyException();
    }

    @Test
    void 나의_Comment_목록을_조회한다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);

        User commenter = UserFixture.createUser();
        User savedCommenter = userRepository.save(commenter);

        String token = jwtTokenManager.createAccessToken(savedCommenter.getId(), savedCommenter.getEmail());

        Moment moment = new Moment("오늘 하루는 힘든 하루~", savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Comment savedComment = commentCreatedAtHelper.saveCommentWithCreatedAt("첫 번째 댓글", savedCommenter,
                savedMoment.getId(), start);

        Moment moment2 = new Moment("오늘 하루는 즐거운 하루~", savedMomenter);
        Moment savedMoment2 = momentRepository.save(moment2);

        Comment savedComment2 = commentCreatedAtHelper.saveCommentWithCreatedAt("즐거운 댓글", savedCommenter,
                savedMoment2.getId(), start.plusHours(1));

        // when
        MyCommentPageResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .param("limit", 1)
                .when().get("/api/v2/comments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyCommentPageResponse.class);

        // then
        List<MyCommentResponse> myComments = response.items().myCommentsResponse();
        MyCommentResponse firstResponse = myComments.getFirst();

        String cursor = savedComment2.getCreatedAt().toString() + "_" + savedComment2.getId();

        assertAll(
                () -> assertThat(myComments).hasSize(1),
                () -> assertThat(response.nextCursor()).isEqualTo(cursor),
                () -> assertThat(response.hasNextPage()).isTrue(),
                () -> assertThat(response.pageSize()).isEqualTo(1),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment2.getContent()),
                () -> assertThat(firstResponse.content()).isEqualTo(savedComment2.getContent()),
                () -> assertThat(firstResponse.moment().content()).isEqualTo(savedMoment2.getContent())
        );
    }

    @Test
    void 나의_Comment_목록을_조회시_삭제된_모멘트는_비어있다() {
        // given
        User momenter = UserFixture.createUser();
        User savedMomenter = userRepository.saveAndFlush(momenter);

        User commenter = UserFixture.createUser();
        User savedCommenter = userRepository.saveAndFlush(commenter);

        String token = jwtTokenManager.createAccessToken(savedCommenter.getId(), savedCommenter.getEmail());

        Moment moment = new Moment("오늘 하루는 힘든 하루~", savedMomenter);
        Moment savedMoment = momentRepository.saveAndFlush(moment);

        Comment comment = new Comment("첫 번째 댓글", savedCommenter, savedMoment.getId());
        Comment savedComment = commentRepository.saveAndFlush(comment);

        // when
        momentRepository.delete(savedMoment);

        MyCommentPageResponse response = RestAssured.given().log().all()
                .cookie("accessToken", token)
                .param("limit", 1)
                .when().get("/api/v2/comments/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", MyCommentPageResponse.class);

        // then
        List<MyCommentResponse> myComments = response.items().myCommentsResponse();
        MyCommentResponse commentOfDeletedMomentResponse = myComments.stream()
                .filter(myCommentResponse -> Objects.equals(myCommentResponse.id(), savedComment.getId()))
                .findFirst()
                .get();

        assertThat(commentOfDeletedMomentResponse.moment()).isNull();
    }

    @Test
    void 코멘트를_신고한다() {
        // given
        User momenter = UserFixture.createUser();
        User commenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);
        User savedCommenter = userRepository.save(commenter);

        Moment moment = new Moment("아 행복해", savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        Comment comment = new Comment("아 행복해", savedCommenter, savedMoment.getId());

        String token = jwtTokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        CommentReportCreateRequest request = new CommentReportCreateRequest("SEXUAL_CONTENT");

        // when
        CommentReportCreateResponse commentReportCreateResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("api/v1/comments/1/reports")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getObject("data", CommentReportCreateResponse.class);

        // then
        assertThat(commentReportCreateResponse.id()).isEqualTo(1L);
    }

    @Test
    void 신고된_코멘트를_삭제한다() {
        // given
        User momenter = UserFixture.createUser();
        User commenter = UserFixture.createUser();
        User savedMomenter = userRepository.save(momenter);
        User savedCommenter = userRepository.save(commenter);

        Moment moment = new Moment("아 행복해", savedMomenter);
        Moment savedMoment = momentRepository.save(moment);

        Comment comment = new Comment("아 행복해", savedCommenter, savedMoment.getId());
        Comment savedComment = commentRepository.save(comment);

        String token = jwtTokenManager.createAccessToken(savedMomenter.getId(), savedMomenter.getEmail());

        CommentReportCreateRequest request = new CommentReportCreateRequest("SEXUAL_CONTENT");

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .body(request)
                .when().post("api/v1/comments/" + savedComment.getId() + "/reports")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        // then
        Optional<Comment> findComment = commentRepository.findById(savedComment.getId());
        Optional<CommentImage> findCommentImage = commentImageRepository.findByComment(savedComment);

        assertThat(findComment).isEmpty();
        assertThat(findCommentImage).isEmpty();
    }
}
