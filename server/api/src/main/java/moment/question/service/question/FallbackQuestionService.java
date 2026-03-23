package moment.question.service.question;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.question.domain.FallbackQuestion;
import moment.question.infrastructure.FallbackQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FallbackQuestionService {

    private final FallbackQuestionRepository fallbackRepository;

    public FallbackQuestion getUnusedFallbackContent() {
        return fallbackRepository.findFirstByIsUsedFalse()
                .orElseThrow(() -> new MomentException(ErrorCode.QUESTION_FALLBACK_EXHAUSTED));
    }

    @Transactional
    public void markAsUsed(Long fallbackQuestionId) {
        FallbackQuestion fallbackQuestion = fallbackRepository.findById(fallbackQuestionId)
                .orElseThrow(() -> new MomentException(ErrorCode.QUESTION_FALLBACK_NOT_FOUND));
        fallbackQuestion.markAsUsed();
    }
}
