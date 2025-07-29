package moment.moment.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void 코멘터에게_오늘_매칭된_모멘트를_조회한다() {
        // given
        User commenter = new User("mimi@icloud.com", "1234", "mimi");
        User momenter = new User("cookie@icloud.com", "1234", "cookie");
        Moment expect = new Moment("집가고 싶어",  momenter);
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        given(momentRepository.findMatchedMomentByCommenter(any(User.class), eq(today), eq(tomorrow)))
                .willReturn(Optional.of(expect));

        // when
        Optional<Moment> result = defaultMomentQueryService.findTodayMatchedMomentByCommenter(commenter);

        // then
        assertAll(
                () -> assertThat(result).isNotEmpty(),
                () -> assertThat(result.get().getContent()).isEqualTo(expect.getContent()),
                () -> assertThat(result.get().checkMomenter(momenter)).isTrue()
        );
    }
}
