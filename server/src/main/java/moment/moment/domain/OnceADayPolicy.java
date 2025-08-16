package moment.moment.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("onceADayPolicy")
@RequiredArgsConstructor
@Primary
public class OnceADayPolicy implements MomentCreatePolicy {

    private final MomentRepository momentRepository;

    @Override
    public boolean canCreate(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        int todayMomentCount = momentRepository.countByMomenterAndWriteTypeAndCreatedAtBetween(
                user,
                WriteType.BASIC,
                startOfDay,
                endOfDay);

        return todayMomentCount < 1;
    }
}
