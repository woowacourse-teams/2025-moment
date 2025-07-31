package moment.matching.application;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.matching.domain.Matching;
import moment.matching.domain.MatchingResult;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RandomMatchingService implements MatchingService {

    private final MatchingRepository matchingRepository;
    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;

    @Override
    @Transactional
    public MatchingResult match(Long momentId) {
        Moment moment = momentQueryService.getMomentById(momentId);
        if (moment.alreadyMatched()) {
            log.warn("ALREADY_MATCHED: [{}]", momentId);
            return MatchingResult.ALREADY_MATCHED;
        }
        User momenter = userQueryService.getUserById(moment.getMomenterId());

        List<User> todayNonMatchedUser = userQueryService.findNotMatchedUsersTodayByMomenter(momenter);

        if (todayNonMatchedUser.isEmpty()) {
            log.warn("NO_AVAILABLE_USERS: [{}]", momentId);
            return MatchingResult.NO_AVAILABLE_USERS;
        }

        int randomNumber = ThreadLocalRandom.current().nextInt(0, todayNonMatchedUser.size());
        User commenter = todayNonMatchedUser.get(randomNumber);

        Matching matching = new Matching(moment, commenter);
        matchingRepository.save(matching);

        moment.matchComplete();
        log.info("MATCHED: [{}]", momentId);
        return MatchingResult.MATCHED;
    }
}
