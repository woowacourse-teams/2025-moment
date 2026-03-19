package moment.question.domain;

import java.util.List;

public interface QuestionGenerator {

    String generate(List<String> recentQuestions);
}
