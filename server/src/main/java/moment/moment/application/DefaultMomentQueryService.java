package moment.moment.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultMomentQueryService implements MomentQueryService {

    private final MomentRepository momentRepository;

    @Override
    public Moment getMomentById(Long id) {
        return momentRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.MOMENT_NOT_FOUND));
    }

    @Override
    public Optional<Moment> findTodayMatchedMomentByCommenter(User commenter) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        return momentRepository.findMatchedMomentByCommenter(commenter, startOfDay, endOfDay);
    }
}
