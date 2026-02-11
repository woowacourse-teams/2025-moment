package moment.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE admins SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Admin extends BaseEntity {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 15)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminRole role;

    private LocalDateTime deletedAt;

    public Admin(String email, String name, String password) {
        this(email, name, password, AdminRole.ADMIN);
    }

    public Admin(String email, String name, String password, AdminRole role) {
        validate(email, password, name);
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
    }


    private void validate(String email, String password, String nickname) {
        validateEmail(email);
        validatePassword(password);
        validateName(nickname);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 email 형식입니다.");
        }

        Matcher matcher = EMAIL_REGEX.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("유효하지 않은 email 형식입니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 password 형식입니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 name 형식입니다.");
        }

        if (name.length() > 15 || name.length() < 2) {
            throw new IllegalArgumentException("유효하지 않은 name 형식입니다.");
        }
    }

    public boolean isSuperAdmin() {
        return this.role == AdminRole.SUPER_ADMIN;
    }

    public boolean canRegisterAdmin() {
        return this.role.canRegisterAdmin();
    }

    /**
     * 관리자가 차단되었는지 확인
     * @return deletedAt이 NULL이 아니면 true
     */
    public boolean isBlocked() {
        return this.getDeletedAt() != null;
    }
}
