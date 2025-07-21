package moment.moment.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
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
class DefaultMomentQueryServiceTest {

    @InjectMocks
    private DefaultMomentQueryService defaultMomentQueryService;

    @Mock
    private MomentRepository momentRepository;

    @Test
    void 모멘트를_ID로_조회한다() {
        // given
        User user = new User("harden@gmail.com", "1234", "하든");
        Moment moment = new Moment("야근 힘들어용 ㅠㅠ", user);
        given(momentRepository.findById(any(Long.class))).willReturn(Optional.of(moment));

        // when
        defaultMomentQueryService.getMomentById(1L);

        // then
        then(momentRepository).should(times(1)).findById(any(Long.class));
    }
}
