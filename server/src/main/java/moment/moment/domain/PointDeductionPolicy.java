package moment.moment.domain;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reward.domain.Reason;
import moment.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class PointDeductionPolicy implements ExtraMomentCreatePolicy {

    @Override
    public void validate(User user) {
        boolean canNotUse = user.canNotUseStars(Reason.MOMENT_ADDITIONAL_USE.getPointTo());

        if (canNotUse) {
            throw new MomentException(ErrorCode.USER_NOT_ENOUGH_STAR);
        }
    }
}
