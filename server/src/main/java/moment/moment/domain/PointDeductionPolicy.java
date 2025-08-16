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
        // TODO : user 포인트를 확인할 수 있는 코드가 삽입되면 해당 코드 재사용
        return user.getCurrentPoint() + Reason.MOMENT_ADDITIONAL_USE.getPointTo() >= 0;
    }
}
