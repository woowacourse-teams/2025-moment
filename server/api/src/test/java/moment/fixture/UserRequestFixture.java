package moment.fixture;

import java.util.UUID;
import moment.user.dto.request.UserCreateRequest;

public class UserRequestFixture {

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(getEmail(), "password123!@#", "password123!@#", getNickname());
    }

    public static UserCreateRequest createUserCreateRequestByEmail(String email) {
        return new UserCreateRequest(email, "password123!@#", "password123!@#", getNickname());
    }

    public static UserCreateRequest createUserCreateRequestByNickname(String nickname) {
        return new UserCreateRequest(getEmail(), "password", "password", nickname);
    }

    public static UserCreateRequest createUserCreateRequestByPassword(String password, String checkedPassword) {
        return new UserCreateRequest(getEmail(), password, checkedPassword, getNickname());
    }

    private static String getEmail() {
        UUID uuid = UUID.randomUUID();
        return String.format("%s@email.com", uuid);
    }

    private static String getNickname() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 8);
    }
}
