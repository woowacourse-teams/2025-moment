package moment.moment.application;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
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
    public Moment getMomentWithMomenterById(Long id) {
        return momentRepository.findWithMomenterByid(id)
                .orElseThrow(() -> new MomentException(ErrorCode.MOMENT_NOT_FOUND));
    }
}
