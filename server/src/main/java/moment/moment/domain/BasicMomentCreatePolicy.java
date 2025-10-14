package moment.moment.domain;

import moment.user.domain.User;

public interface BasicMomentCreatePolicy {

    void validate(User user);

    boolean canCreate(User user);
}
