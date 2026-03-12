package moment.question.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import moment.question.domain.Question;

@Schema(description = "현재 질문 응답")
public record QuestionResponse(
        @Schema(description = "질문 내용", example = "오늘의 기분은 어떠세요~~~?")
        String content
) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getContent());
    }
}
