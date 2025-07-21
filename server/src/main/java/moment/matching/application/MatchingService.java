package moment.matching.application;

import moment.matching.domain.MatchingResult;

public interface MatchingService {
    MatchingResult match(Long momentId);
}
