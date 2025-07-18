package moment.moment.application;

import lombok.RequiredArgsConstructor;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MomentService {

    private final MomentRepository momentRepository;
    private final UserQueryService userQueryService;

    public MomentCreateResponse addMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        Moment momentWithoutId = new Moment(request.content(), momenter);

        Moment moment = momentRepository.save(momentWithoutId);

        return MomentCreateResponse.of(moment);
    }
}
