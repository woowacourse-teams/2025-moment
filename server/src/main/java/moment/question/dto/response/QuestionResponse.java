package moment.question.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import moment.question.domain.Question;

@Schema(description = "현재 질문 응답")
public record QuestionResponse(
        @Schema(description = "질문 ID", example = "1")
        Long id,

        @Schema(description = "질문 내용", example = "오늘의 기분은 어떠세요~~~?")
        String content,

        @Schema(description = "시작일", example = "2026-03-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2026-03-06")
        LocalDate endDate
) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getContent(), question.getStartDate(),
                question.getEndDate());
    }
}
