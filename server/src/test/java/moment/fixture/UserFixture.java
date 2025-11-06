package moment.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.UserCreateRequest;

public class UserFixture {

    public static User createUser() {
        return new User(getEmail(), "password123!@#", getNickname(), ProviderType.EMAIL);
    }

    public static User createGoogleUser() {
        return new User(getEmail(), "password123!@#", getNickname(), ProviderType.GOOGLE);
    }

    public static User createUserByEmail(String email) {
        return new User(email, "password123!@#", getNickname(), ProviderType.EMAIL);
    }

    public static User createUserByPassword(String password) {
        return new User(getEmail(), password, getNickname(), ProviderType.EMAIL);
    }

    public static User createUserByNickname(String nickname) {
        return new User(getEmail(), "password1231@#", nickname, ProviderType.EMAIL);
    }

    public static User createUserByEmailAndNickname(String email, String nickname) {
        return new User(email, "password1231@#", nickname, ProviderType.EMAIL);
    }

    public static List<User> createUsersByAmount(int amount) {
        List<User> users = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            int userNum = i + 1;
            User user = createUserByEmailAndNickname(
                    getEmail(),
                    getNickname()
            );
            users.add(user);
        }
        return users;
    }

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
