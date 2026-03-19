package moment.support;

import java.util.ArrayList;
import java.util.List;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.QuestionGenerator;


public class FakeQuestionGenerator implements QuestionGenerator {
    private String nextQuestion = "";
    private List<String> lastReceivedQuestions = new ArrayList<>();
    private boolean shouldThrowException = false; // 예외 발생 스위치

    @Override
    public String generate(List<String> recentQuestions) {
        this.lastReceivedQuestions = new ArrayList<>(recentQuestions);

        if (shouldThrowException) {
            throw new MomentException(ErrorCode.AI_API_ERROR);
        }
        
        return nextQuestion;
    }

    public void setNextQuestion(String nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    public void setShouldThrowException(boolean shouldThrowException) {
        this.shouldThrowException = shouldThrowException;
    }

    public List<String> getLastReceivedQuestions() {
        return lastReceivedQuestions;
    }

    // 여러 테스트가 하나의 Fake 빈을 공유할 경우를 대비한 초기화 메서드
    public void clear() {
        this.nextQuestion = "기본 테스트용 질문입니다.";
        this.shouldThrowException = false;
        this.lastReceivedQuestions.clear();
    }
}
