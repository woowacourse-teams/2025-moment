package moment.matching.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import moment.matching.domain.MatchingResult;
import moment.matching.infrastructure.MatchingRepository;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class RandomMatchingServiceTest {

    @InjectMocks
    RandomMatchingService randomMatchingService;

    @Mock
    MomentQueryService momentQueryService;

    @Mock
    UserQueryService userQueryService;

    @Mock
    MatchingRepository matchingRepository;

    @Test
    void 코멘터로_선정되지_않은_사용자와_매칭된다() {
        // given
        User momenter = new User("mimi@icloud.com", "password1234!", "mimi");
        Moment moment = new Moment("내용", false, momenter);
        given(momentQueryService.getMomentById(any())).willReturn(moment);

        User notMatchedUser = new User("hippo@gmail.com", "password1234!", "hippo");
        given(userQueryService.findNotMatchedUsersTodayByMomenter(any())).willReturn(List.of(notMatchedUser));

        // when
        MatchingResult matchingResult = randomMatchingService.match(1L);

        // then
        assertThat(matchingResult).isEqualTo(MatchingResult.MATCHED);
    }

    @Test
    void 모멘트가_이미_매칭된_경우_매칭되지_않는다() {
        // given
        User momenter = new User("mimi@icloud.com", "password1234!", "mimi");
        Moment alreadyMatchedMoment = new Moment("내용", true, momenter);
        given(momentQueryService.getMomentById(any())).willReturn(alreadyMatchedMoment);

        // when
        MatchingResult matchingResult = randomMatchingService.match(1L);

        // then
        assertThat(matchingResult).isEqualTo(MatchingResult.ALREADY_MATCHED);
    }

    @Test
    void 매칭을_할_사용자가_없는_경우_매칭되지_않는다() {
        // given
        User momenter = new User("mimi@icloud.com", "password1234!", "mimi");
        Moment moment = new Moment("내용", false, momenter);
        given(momentQueryService.getMomentById(any())).willReturn(moment);

        given(userQueryService.findNotMatchedUsersTodayByMomenter(any())).willReturn(Collections.emptyList());

        // when
        MatchingResult matchingResult = randomMatchingService.match(1L);

        // then
        assertThat(matchingResult).isEqualTo(MatchingResult.NO_AVAILABLE_USERS);
    }
}
