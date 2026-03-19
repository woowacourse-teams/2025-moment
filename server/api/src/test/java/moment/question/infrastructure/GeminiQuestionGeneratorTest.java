package moment.question.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.QuestionGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(
        components = GeminiQuestionGenerator.class,
        properties = {
                "ai.gemini.api-key=fake-test-key",
                "ai.gemini.url=https://api.test-gemini.com/generate",
                "ai.retry.delay=10",
                "ai.retry.multiplier=1.0"
        }
)
@MockitoBean(types = JpaMetamodelMappingContext.class) // 가짜 메타모델
class GeminiQuestionGeneratorTest {

    @Autowired
    private QuestionGenerator generator;

    @Autowired
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 테스트 간 격리를 위해 Mock 서버 초기화
        mockServer.reset();
    }

    @Test
    void 제미니_API_정상_호출_및_파싱_성공() {
        // given
        String fakeGeminiJsonResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "\\"오늘 하루 중 가장 숨기고 싶었던 찌질한 감정은 무엇이었나요?\\""
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("https://api.test-gemini.com/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(fakeGeminiJsonResponse, MediaType.APPLICATION_JSON));

        // when
        String result = generator.generate(List.of("이전 질문 1", "이전 질문 2"));

        assertThat(result).isEqualTo("오늘 하루 중 가장 숨기고 싶었던 찌질한 감정은 무엇이었나요?");
        mockServer.verify(); // 우리가 설정한 Mock API가 실제로 호출되었는지 검증
    }

    @Test
    void 제미니_API_5회_실패시_Recover_동작하여_예외발생() {
        // given
        mockServer.expect(ExpectedCount.times(5), requestTo("https://api.test-gemini.com/generate"))
                .andRespond(withServerError());

        // when & then
        assertThatThrownBy(() -> generator.generate(List.of("테스트 질문")))
                .isInstanceOf(MomentException.class)
                .hasMessage(ErrorCode.AI_API_ERROR.getMessage());

        mockServer.verify();
    }
}