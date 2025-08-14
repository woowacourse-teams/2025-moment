package moment.moment.domain;

import lombok.RequiredArgsConstructor;
import moment.user.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Primary
public class PointDeductionPolicy implements ExtraMomentCreatePolicy {

    private static final int EXTRA_MOMENT_COST = 10;

    @Override
    public boolean canCreate(User user) {
        return user.getCurrentPoint() >= EXTRA_MOMENT_COST;
    }
}
