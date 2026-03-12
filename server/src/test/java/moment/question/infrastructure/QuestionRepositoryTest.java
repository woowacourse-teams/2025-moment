package moment.question.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.Optional;
import moment.config.TestTags;
import moment.global.domain.QuestionType;
import moment.question.domain.Question;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void 날짜와_질문_타입으로_질문을_찾는다() {
        // given
        String content = "공통 질문";
        LocalDate monday = LocalDate.of(2026, 3, 9);
        LocalDate sunday = monday.plusDays(6);
        questionRepository.save(
                new Question(content, QuestionType.COMMON, monday, sunday, null)
        );

        // when
        Optional<Question> question = questionRepository
                .findByStartDateAndEndDateAndQuestionType(monday, sunday, QuestionType.COMMON);

        // then
        assertAll(
                () -> assertThat(question).isPresent(),
                () -> assertThat(question.get().getContent()).isEqualTo(content)
        );
    }

    @Test
    void 날짜와_질문_타입과_그룹ID로_질문을_찾는다() {
        // given
        String content = "커스텀 질문";
        Long groupId = 10L;
        LocalDate monday = LocalDate.of(2026, 3, 9);
        LocalDate sunday = monday.plusDays(6);
        questionRepository.save(
                new Question(content, QuestionType.CUSTOM, monday, sunday, groupId)
        );

        // when
        Optional<Question> question = questionRepository
                .findByStartDateAndEndDateAndQuestionTypeAndGroupId(monday, sunday, QuestionType.CUSTOM, groupId);

        // then
        assertAll(
                () -> assertThat(question).isPresent(),
                () -> assertThat(question.get().getContent()).isEqualTo(content)
        );
    }
}
