package moment.moment.domain;

import moment.user.domain.User;

public interface BasicMomentCreatePolicy {

    boolean canCreate(User user);
}
