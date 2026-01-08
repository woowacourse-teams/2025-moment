package moment.admin.domain;

import lombok.Getter;

@Getter
public enum AdminRole {
    SUPER_ADMIN(true),
    ADMIN(false);

    private final boolean canManageAdmins;

    AdminRole(boolean canManageAdmins) {
        this.canManageAdmins = canManageAdmins;
    }

    public boolean canRegisterAdmin() {
        return this.canManageAdmins;
    }
}
