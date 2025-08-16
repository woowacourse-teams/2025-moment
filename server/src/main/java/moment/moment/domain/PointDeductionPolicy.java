package moment.moment.domain;

import lombok.RequiredArgsConstructor;
import moment.reward.domain.Reason;
import moment.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class PointDeductionPolicy implements ExtraMomentCreatePolicy {


    @Override
    public boolean canCreate(User user) {
        return user.getCurrentPoint() + Reason.MOMENT_ADDITIONAL_USE.getPointTo() >= 0;
    }
}
