package moment.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.request.AdminLoginRequest;

public class AdminFixture {

    public static Admin createAdmin() {
        return new Admin(getEmail(), getName(), "password123!@#");
    }

    public static Admin createSuperAdmin() {
        return new Admin(getEmail(), getName(), "password123!@#", AdminRole.SUPER_ADMIN);
    }

    public static Admin createAdminByEmail(String email) {
        return new Admin(email, getName(), "password123!@#");
    }

    public static Admin createAdminByPassword(String password) {
        return new Admin(getEmail(), getName(), password);
    }

    public static Admin createAdminByEmailAndPassword(String email, String password) {
        return new Admin(email, getName(), password);
    }

    public static Admin createAdminByRole(AdminRole role) {
        return new Admin(getEmail(), getName(), "password123!@#", role);
    }

    public static List<Admin> createAdminsByAmount(int amount) {
        List<Admin> admins = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Admin admin = createAdmin();
            admins.add(admin);
        }
        return admins;
    }

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
