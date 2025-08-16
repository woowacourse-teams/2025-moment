package moment.moment.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnceADayPolicyTest {

    @InjectMocks
    private OnceADayPolicy onceADayPolicy;

    @Mock
    private MomentRepository momentRepository;

    @Test
    void 오늘_생성된_기본_모멘트가_없는_경우_참을_반환한다() {
        // given
        User user = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        WriteType basicWriteType = WriteType.BASIC;
        int todayNonExistBasicMomentCount = 0;
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        when(momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(user, basicWriteType, startOfDay, endOfDay))
                .thenReturn(todayNonExistBasicMomentCount);

        // when
        boolean result = onceADayPolicy.canCreate(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 오늘_생성된_기본_모멘트가_있는_경우_거짓을_반환한다() {
        // given
        User user = new User("mimi@icloud.com", "mimi1234!", "미미", ProviderType.EMAIL);
        WriteType basicWriteType = WriteType.BASIC;
        int todayAlreadyExistBasicMomentCount = 1;
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        when(momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(user, basicWriteType, startOfDay, endOfDay))
                .thenReturn(todayAlreadyExistBasicMomentCount);

        // when
        boolean result = onceADayPolicy.canCreate(user);

        // then
        assertThat(result).isFalse();
    }
}
