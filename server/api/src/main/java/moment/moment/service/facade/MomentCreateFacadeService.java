package moment.moment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.service.application.MomentApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentCreateFacadeService {

    private final MomentApplicationService momentApplicationService;

    @Transactional
    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        return momentApplicationService.createBasicMoment(request, momenterId);
    }

    @Transactional
    public MomentCreateResponse createExtraMoment(MomentCreateRequest request, Long momenterId) {
        return momentApplicationService.createExtraMoment(request, momenterId);
    }

}
