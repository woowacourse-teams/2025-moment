package moment.question.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import moment.auth.infrastructure.JwtTokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.global.domain.QuestionType;
import moment.global.domain.TimeProvider;
import moment.question.domain.Question;
import moment.question.dto.response.QuestionResponse;
import moment.question.infrastructure.QuestionRepository;
import moment.support.TimeProviderTestConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@org.junit.jupiter.api.Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
@Import(TimeProviderTestConfig.class) // 해당 시간 설정을 사용합니다.
class QuestionControllerTest {

    private final String dailyContent = "일간질문";
    private final String weeklyContent = "주간질문";
    private final String monthlyContent = "월간질문";

    private User user;
    private String token;

    @LocalServerPort
    private int port;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Autowired
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        LocalDate today = timeProvider.now().toLocalDate();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        LocalDate firstDate = today.withDayOfMonth(1);
        LocalDate lastDate = today.with(TemporalAdjusters.lastDayOfMonth());

        questionRepository.saveAll(
                List.of(
                        new Question(dailyContent, QuestionType.COMMON, today, today, null),
                        new Question(weeklyContent, QuestionType.COMMON, monday, sunday, null),
                        new Question(monthlyContent, QuestionType.COMMON, firstDate, lastDate, null)
                )
        );

        user = userRepository.saveAndFlush(UserFixture.createUser());
        token = jwtTokenManager.createAccessToken(user.getId(), user.getEmail());
    }

    @Test
    void 일간_질문을_조회한다() {
        // when & then
        QuestionResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .when().get("/api/v2/questions/current?groupId=1&cycle=DAILY")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", QuestionResponse.class);

        assertThat(response.content()).isEqualTo(dailyContent);
    }

    @Test
    void 주간_질문을_조회한다() {
        // when & then
        QuestionResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .when().get("/api/v2/questions/current?groupId=1&cycle=WEEKLY")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", QuestionResponse.class);

        assertThat(response.content()).isEqualTo(weeklyContent);
    }

    @Test
    void 월간_질문을_조회한다() {
        // when & then
        QuestionResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("accessToken", token)
                .when().get("/api/v2/questions/current?groupId=1&cycle=MONTHLY")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", QuestionResponse.class);

        assertThat(response.content()).isEqualTo(monthlyContent);
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }
}
