package moment.moment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.service.application.MomentApplicationService;
import moment.reward.service.application.RewardApplicationService;
import moment.reward.domain.Reason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentCreateFacadeService {

    private final MomentApplicationService momentApplicationService;
    private final RewardApplicationService rewardApplicationService;

    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        MomentCreateResponse basicMoment = momentApplicationService.createBasicMoment(request, momenterId);

        rewardApplicationService.rewardForMoment(momenterId, Reason.MOMENT_CREATION, basicMoment.id());
        return basicMoment;
    }

    public MomentCreateResponse createExtraMoment(MomentCreateRequest request, Long momenterId) {
        MomentCreateResponse extraMoment = momentApplicationService.createExtraMoment(request, momenterId);
        rewardApplicationService.useReward(momenterId, Reason.MOMENT_ADDITIONAL_USE, momenterId);
        return extraMoment;
    }

}
