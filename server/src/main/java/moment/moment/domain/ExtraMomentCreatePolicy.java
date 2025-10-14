package moment.moment.domain;

import moment.user.domain.User;

public interface ExtraMomentCreatePolicy {

    void validate(User user);

    boolean canNotCreate(User user);
}
