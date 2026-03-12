package moment.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.global.domain.QuestionType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "questions")
@SQLDelete(sql = "UPDATE questions SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String content;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(nullable = false, updatable = false)
    private LocalDate startDate;

    @Column(nullable = false, updatable = false)
    private LocalDate endDate;

    private Long groupId;

    public Question(String content, QuestionType questionType, LocalDate startDate, LocalDate endDate, Long groupId) {
        validate(content);
        this.content = content;
        this.questionType = questionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupId = groupId;
    }

    private void validate(String content) {
        validateContent(content);
        validateContentLength(content);
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("question의 content는 null이거나 빈 값이어서는 안 됩니다.");
        }
    }

    private void validateContentLength(String content) {
        if (content.length() > 200) {
            throw new IllegalArgumentException("질문은 1자 이상, 200자 이하로만 작성 가능합니다.");
        }
    }
}
