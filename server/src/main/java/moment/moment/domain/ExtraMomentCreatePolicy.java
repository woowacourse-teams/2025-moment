package moment.moment.domain;

import moment.user.domain.User;

public interface ExtraMomentCreatePolicy {

    boolean canCreate(User user);
}
