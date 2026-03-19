package moment.fixture;

import java.util.UUID;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.request.AdminLoginRequest;

public class AdminRequestFixture {

    public static AdminCreateRequest createAdminCreateRequest() {
        return new AdminCreateRequest(getEmail(), getName(), "password123!@#");
    }

    public static AdminCreateRequest createAdminCreateRequestByEmail(String email) {
        return new AdminCreateRequest(email, getName(), "password123!@#");
    }

    public static AdminLoginRequest createAdminLoginRequest(String email, String password) {
        return new AdminLoginRequest(email, password);
    }

    private static String getEmail() {
        UUID uuid = UUID.randomUUID();
        return String.format("%s@admin.com", uuid);
    }

    private static String getName() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 8);
    }
}
