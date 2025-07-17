package moment.user.dto.request;

import moment.user.domain.User;

public record UserCreateRequest(String email, String password, String rePassword, String nickname) {

    public User toUser() {
        return new User(email, password, nickname);
    }
}
