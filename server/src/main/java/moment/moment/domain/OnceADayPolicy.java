package moment.moment.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class OnceADayPolicy implements BasicMomentCreatePolicy {

    private final MomentRepository momentRepository;

    @Override
    public void validate(User user) {
        if (!canCreate(user)) {
            throw new MomentException(ErrorCode.MOMENT_ALREADY_EXIST);
        }
    }

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
