package moment.question.infrastructure;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.QuestionGenerator;
import moment.question.dto.request.GeminiRequest;
import moment.question.dto.response.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GeminiQuestionGenerator implements QuestionGenerator {

    private final RestClient restClient;
    private final String url;

    public GeminiQuestionGenerator(
            RestClient.Builder restClientBuilder,
            @Value("${ai.gemini.api-key}") String apiKey,
            @Value("${ai.gemini.url}") String url) {

        this.url = url;

        this.restClient = restClientBuilder
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    @Retryable(
            retryFor = RuntimeException.class,
            maxAttempts = 5,
            // 💡 상수를 버리고 동적 표현식으로 변경합니다. (기본값: 2000ms, 2.0배)
            backoff = @Backoff(
                    delayExpression = "${ai.retry.delay:2000}",
                    multiplierExpression = "${ai.retry.multiplier:2.0}"
            )
    )
    public String generate(List<String> recentQuestions) {
        log.info("GEMINI 호출 시작");

        String pastQuestions = String.join("\n- ", recentQuestions);
        String prompt = """
                당신은 익명 소셜 다이어리의 따뜻하고 공감 능력이 뛰어난 퍼실리테이터입니다.
                우리 서비스의 핵심 목표는 '익명성'을 방패 삼아 현실에서 체면이나 부끄러움 때문에 차마 말하지 못했던 솔직한 감정, 실패, 상처를 꺼내놓고 서로 깊이 공감하며 유대감을 형성하는 것입니다.
                
                사용자들이 자신의 내면이나 일상의 숨겨둔 감정을 거부감 없이 털어놓고, 다른 사람의 답변을 보며 '나만 그런 게 아니었구나'라고 짙은 위안을 얻을 수 있는 질문을 딱 한 문장으로 생성하세요.
                
                [제약 조건]
                1. 너무 거창하거나 철학적인 논쟁(예: 삶의 의미란?, 성공의 기준은?)은 절대 피하세요.
                2. 누구나 일상에서 남몰래 겪는 찌질한 감정, 소소한 후회, 숨겨둔 다정함 등 구체적이고 감성적인 부분에 집중하세요.
                3. 말투는 다정하고 부드러운 대화체로 끝맺으세요.
                4. 반드시 이전에 했던 다음 질문들과 절대 겹치지 않는 새로운 질문이어야 합니다.
                5. 질문은 반드시 공백 포함 200자 미만으로 생성하세요.
                
                [최근 질문 내역]
                %s
                """.formatted(pastQuestions);

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                        List.of(new GeminiRequest.Part(prompt))
                )),
                new GeminiRequest.GenerationConfig(0.7) // 창의성 수치
        );

        GeminiResponse response = restClient.post()
                .uri(url)
                .body(request)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new RuntimeException("Gemini 응답이 비어있습니다.");
        }

        return response.candidates().getFirst().content().parts().get(0).text().replace("\"", "").trim();
    }

    @Recover
    public String recover(RuntimeException e) {
        log.error("AI 호출 5회 실패. 예외를 전파합니다.", e);
        throw new MomentException(ErrorCode.AI_API_ERROR);
    }
}
