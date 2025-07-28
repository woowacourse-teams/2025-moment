package moment.moment.domain;

import moment.user.domain.User;

public interface MomentCreatePolicy {

    boolean canCreate(User user);
}
